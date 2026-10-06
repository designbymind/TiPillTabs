// TiPillTabs: native Android view. It draws the items of the proxy as a Material chip group.
// Only the selected chip shows its title. A selection change animates the widths of the chips.
// MIT license. See the LICENSE file.

package ti.pilltabs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import androidx.core.graphics.ColorUtils;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.RelativeCornerSize;

import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiDrawableReference;
import org.appcelerator.titanium.view.TiUIView;

// A single-line, single-selection ChipGroup in a HorizontalScrollView.
// This is the layout that the Material guidelines give for a row of chips that is wider than the screen.
final class PillTabsView extends TiUIView
{
	private static final String TAG = "TiPillTabs";
	private static final int[][] CHECKED_THEN_DEFAULT = { { android.R.attr.state_checked }, {} };
	private static final float DEFAULT_SPACING_DP = 8;
	private static final float ICON_SIZE_DP = 18;
	private static final float MIN_ICON_INSET_DP = 8;
	private static final long DEFAULT_ANIMATION_DURATION_MS = 300;

	private final ViewProxy pillProxy;
	private final HorizontalScrollView scrollView;
	private final ChipGroup group;
	private final Map<String, Chip> chips = new HashMap<>();
	// True when the selected chip must scroll into view at the next layout pass.
	private boolean revealPending;
	// True when that scroll must be smooth, because the chips animate at the same time.
	private boolean revealSmooth;
	// The space between the icon and the start edge of its chip, in pixels.
	// A chip that shows only its icon has the same space at its end edge.
	private float iconInset;
	// The paddings of the stock chip style for a chip that shows its title.
	private float textStartPadding;
	private float textEndPadding;
	private float chipEndPadding;
	// The `iconFamily` of the view. An item uses it when the item has no `iconFamily`.
	private Object iconFamily;

	PillTabsView(ViewProxy proxy)
	{
		super(proxy);
		pillProxy = proxy;
		Context context = proxy.getActivity();

		group = new ChipGroup(context);
		group.setSingleLine(true);
		group.setSingleSelection(true);
		group.setSelectionRequired(true);

		scrollView = new HorizontalScrollView(context) {
			@Override
			protected void onConfigurationChanged(Configuration newConfig)
			{
				super.onConfigurationChanged(newConfig);
				// The colors from the theme can change with the light or dark appearance.
				post(PillTabsView.this::rebuild);
			}

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
			{
				// The chips fill the height of the view. Without a fixed height, they have the Material height.
				applyIconInset(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY
					? MeasureSpec.getSize(heightMeasureSpec) : 0);
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			}

			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom)
			{
				super.onLayout(changed, left, top, right, bottom);
				if (revealPending) {
					revealPending = false;
					reveal(chips.get(pillProxy.getSelectedId()), revealSmooth);
				}
			}
		};
		scrollView.setHorizontalScrollBarEnabled(false);
		scrollView.addView(group, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		setNativeView(scrollView);

		iconInset = dp(MIN_ICON_INSET_DP);
		applySpacing(proxy.getProperty(ViewProxy.PROPERTY_SPACING));
		iconFamily = proxy.getProperty(ViewProxy.PROPERTY_ICON_FAMILY);
		rebuild();
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{
		if (ViewProxy.PROPERTY_SPACING.equals(key)) {
			applySpacing(newValue);
		} else if (ViewProxy.PROPERTY_ICON_FAMILY.equals(key)) {
			iconFamily = newValue;
			rebuild();
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}

	// Creates one chip for each item of the proxy, then shows the selection of the proxy.
	void rebuild()
	{
		// A running animation holds the old chips.
		TransitionManager.endTransitions(scrollView);
		group.removeAllViews();
		chips.clear();
		for (PillItem item : pillProxy.pillItems()) {
			Chip chip = createChip(item);
			chips.put(item.id, chip);
			// The chip fills the height of the view, as the pill does on iOS.
			group.addView(chip, new ChipGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		}
		showSelection(false);
	}

	// The proxy calls this method after a selection change.
	void updateSelection()
	{
		showSelection(true);
	}

	// Checks the selected chip and shows its title. The other chips with an icon show only the icon.
	private void showSelection(boolean animate)
	{
		long duration = animate && scrollView.isLaidOut() ? animationDuration() : 0;
		if (duration > 0) {
			// The transition animates each chip from its old bounds to the bounds of the next layout pass.
			// It obeys the system configuration that removes animations.
			Transition transition = new ChangeBounds();
			transition.setDuration(duration);
			transition.setInterpolator(AnimationUtils.loadInterpolator(
				group.getContext(), android.R.interpolator.fast_out_slow_in));
			TransitionManager.beginDelayedTransition(scrollView, transition);
		}
		final Chip chip = chips.get(pillProxy.getSelectedId());
		if (chip == null) {
			group.clearCheck();
		} else {
			group.check(chip.getId());
		}
		for (Chip each : chips.values()) {
			applyLabel(each);
		}
		if (chip == null) {
			return;
		}
		if (scrollView.isLaidOut() && !scrollView.isLayoutRequested() && !group.isLayoutRequested()) {
			reveal(chip, true);
		} else {
			// The chip has no final position before the layout pass. onLayout() scrolls to it.
			revealPending = true;
			revealSmooth = duration > 0;
		}
	}

	// `animated` and `animationDuration` have the defaults of the iOS module: true and 300 milliseconds.
	// Returns 0 when the selection change must not animate.
	private long animationDuration()
	{
		if (!TiConvert.toBoolean(pillProxy.getProperty(ViewProxy.PROPERTY_ANIMATED), true)) {
			return 0;
		}
		Object value = pillProxy.getProperty(ViewProxy.PROPERTY_ANIMATION_DURATION);
		double duration = value instanceof Number ? ((Number) value).doubleValue() : DEFAULT_ANIMATION_DURATION_MS;
		if (Double.isNaN(duration) || Double.isInfinite(duration)) {
			return DEFAULT_ANIMATION_DURATION_MS;
		}
		return Math.max(0, Math.round(duration));
	}

	// Shows the title on the selected chip, and on a chip without an icon.
	// A chip with only an icon has no text paddings and the same space at both sides of the icon.
	private void applyLabel(Chip chip)
	{
		PillItem item = (PillItem) chip.getTag();
		boolean hasIcon = chip.getChipIcon() != null;
		boolean showTitle = !hasIcon || chip.isChecked();
		if (hasIcon) {
			// The start side is the same in both states, so the icon does not move when the width changes.
			chip.setIconStartPadding(Math.max(0, iconInset - chip.getChipStartPadding()));
			chip.setTextStartPadding(showTitle ? textStartPadding : 0);
			chip.setTextEndPadding(showTitle ? textEndPadding : 0);
			chip.setChipEndPadding(showTitle ? chipEndPadding : iconInset);
		}
		String text = showTitle ? item.title : "";
		if (!text.contentEquals(chip.getText())) {
			chip.setText(text);
		}
	}

	// An icon-only chip is a circle when the icon inset is half of the free height.
	private void applyIconInset(int height)
	{
		float inset = Math.max(dp(MIN_ICON_INSET_DP), (height - dp(ICON_SIZE_DP)) / 2);
		if (inset == iconInset) {
			return;
		}
		iconInset = inset;
		for (Chip chip : chips.values()) {
			applyLabel(chip);
		}
	}

	// Scrolls the minimum distance that makes the chip fully visible.
	private void reveal(Chip chip, boolean smooth)
	{
		if (chip == null || chip.getParent() != group) {
			return;
		}
		int visibleStart = scrollView.getScrollX();
		int target = visibleStart;
		if (chip.getLeft() < visibleStart) {
			target = chip.getLeft();
		} else if (chip.getRight() > visibleStart + scrollView.getWidth()) {
			target = chip.getRight() - scrollView.getWidth();
		}
		if (target == visibleStart) {
			return;
		}
		if (smooth) {
			scrollView.smoothScrollTo(target, 0);
		} else {
			scrollView.scrollTo(target, 0);
		}
	}

	private Chip createChip(final PillItem item)
	{
		Context context = group.getContext();
		Chip chip = new Chip(context);
		// The choice chip is the stock Material chip for single selection. It shows no check mark.
		chip.setChipDrawable(ChipDrawable.createFromAttributes(
			context, null, 0, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice));
		chip.setId(View.generateViewId());
		chip.setTag(item);
		chip.setText(item.title);
		// A chip that shows only its icon has no text for the screen reader.
		chip.setContentDescription(item.title);
		textStartPadding = chip.getTextStartPadding();
		textEndPadding = chip.getTextEndPadding();
		chipEndPadding = chip.getChipEndPadding();
		// The Titanium height of the view sets the touch target. Do not add the 48dp inset of Material.
		chip.setEnsureMinTouchTargetSize(false);
		chip.setShapeAppearanceModel(chip.getShapeAppearanceModel().withCornerSize(new RelativeCornerSize(0.5f)));

		// The defaults come from the app theme. An item color overrides its default.
		// A theme text color only fits the theme background. Thus the default text color
		// for a background color of the item is black or white, the one with more contrast.
		int onSurface = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
		int background = color(item, "backgroundColor", ColorUtils.setAlphaComponent(onSurface, 0x1F));
		int tint = color(item, "tintColor", ColorUtils.setAlphaComponent(
			item.properties.get("backgroundColor") != null ? contrastColor(background) : onSurface, 0x99));
		int activeBackground = color(item, "activeBackgroundColor",
			MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, Color.BLUE));
		int activeTint = color(item, "activeTintColor",
			item.properties.get("activeBackgroundColor") != null
				? contrastColor(activeBackground)
				: MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE));
		ColorStateList foreground = new ColorStateList(CHECKED_THEN_DEFAULT, new int[] { activeTint, tint });
		chip.setChipBackgroundColor(
			new ColorStateList(CHECKED_THEN_DEFAULT, new int[] { activeBackground, background }));
		chip.setTextColor(foreground);

		// `icon` is a glyph of an icon font. `image` is the alternative when the item has no usable glyph.
		Drawable icon = createGlyph(item);
		if (icon == null) {
			icon = loadIcon(item.properties.get("image"));
		}
		if (icon != null) {
			chip.setChipIcon(icon);
			chip.setChipIconVisible(true);
			chip.setChipIconTint(foreground);
			chip.setChipIconSize(dp(ICON_SIZE_DP));
		}

		// The chip group already moved the check mark state. The proxy records the selection and fires `change`.
		chip.setOnClickListener(view -> pillProxy.select(item.id, "tap"));
		return chip;
	}

	// Draws the `icon` text of an item with its icon font into a bitmap of the icon size.
	// The bitmap is black on transparent. The chip tints it with the tint colors of the item.
	private Drawable createGlyph(PillItem item)
	{
		Object glyph = item.properties.get("icon");
		if (!(glyph instanceof String) || ((String) glyph).isEmpty()) {
			return null;
		}
		String text = (String) glyph;
		Object family = item.properties.get("iconFamily");
		if (family == null) {
			family = iconFamily;
		}
		// Titanium finds a custom font by its file name in the "fonts" directory of the app.
		Typeface typeface = family != null
			? TiUIHelper.toTypeface(group.getContext(), TiConvert.toString(family)) : Typeface.DEFAULT;

		int size = Math.round(dp(ICON_SIZE_DP));
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTypeface(typeface);
		paint.setColor(Color.BLACK);
		paint.setTextSize(size);
		// A missing font file gives the system font, which has no glyph for the code point of an icon.
		if (text.codePointCount(0, text.length()) == 1 && !paint.hasGlyph(text)) {
			Log.w(TAG, "The font " + family + " has no glyph for the icon of item " + item.id);
			return null;
		}
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int largest = Math.max(bounds.width(), bounds.height());
		if (largest == 0) {
			return null;
		}
		if (largest > size) {
			// The glyph is larger than its em square. Make it fit the icon size.
			paint.setTextSize(size * (float) size / largest);
			paint.getTextBounds(text, 0, text.length(), bounds);
		}
		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		new Canvas(bitmap).drawText(text,
			(size - bounds.width()) / 2f - bounds.left, (size - bounds.height()) / 2f - bounds.top, paint);
		return new BitmapDrawable(group.getResources(), bitmap);
	}

	// `image` is the Android icon of an item: a local image path or a drawable resource ID.
	private Drawable loadIcon(Object image)
	{
		if (image == null) {
			return null;
		}
		TiDrawableReference reference = TiDrawableReference.fromObject(pillProxy, image);
		if (reference.isNetworkUrl()) {
			Log.w(TAG, "Item image must be a local image; ignoring " + image);
			return null;
		}
		Drawable drawable = reference.getDrawable();
		if (drawable == null) {
			Log.w(TAG, "Could not load item image " + image);
			return null;
		}
		return drawable.mutate();
	}

	private int color(PillItem item, String key, int fallback)
	{
		Object value = item.properties.get(key);
		return value != null ? TiConvert.toColor(value, group.getContext()) : fallback;
	}

	private static int contrastColor(int background)
	{
		return MaterialColors.isColorLight(background) ? Color.BLACK : Color.WHITE;
	}

	// A plain number uses the default unit of the app. Invalid values use 8dp, the default of the iOS module.
	private void applySpacing(Object value)
	{
		TiDimension dimension = value != null ? TiConvert.toTiDimension(value, TiDimension.TYPE_WIDTH) : null;
		int pixels = dimension != null ? dimension.getAsPixels(scrollView) : -1;
		group.setChipSpacingHorizontal(pixels >= 0 ? pixels : Math.round(dp(DEFAULT_SPACING_DP)));
	}

	private float dp(float value)
	{
		return TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, value, group.getResources().getDisplayMetrics());
	}
}
