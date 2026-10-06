// TiPillTabs: Titanium view proxy for Android. It has the same JavaScript API as the iOS module.
// MIT license. See the LICENSE file.

package ti.pilltabs;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

// The proxy owns the items and the selection. The view only draws them.
// Thus the state stays correct before Titanium creates the native view, and after Titanium releases it.
@Kroll.proxy(creatableInModule = TiPilltabsModule.class,
	propertyAccessors = {
		ViewProxy.PROPERTY_SPACING,
		ViewProxy.PROPERTY_ICON_FAMILY,
		ViewProxy.PROPERTY_ANIMATED,
		ViewProxy.PROPERTY_ANIMATION_DURATION,
		// The stock Material chip group has no equivalent for the properties below.
		// The proxy accepts and stores them, so the same JavaScript runs on iOS and Android.
		"aggregateId", "gestureEnabled", "toggleOnReselect", "trailingVisibility"
	})
public class ViewProxy extends TiViewProxy
{
	static final String PROPERTY_SPACING = "spacing";
	// The default icon font for the `icon` of each item. An item can set its own `iconFamily`.
	static final String PROPERTY_ICON_FAMILY = "iconFamily";
	// The view reads these two properties at each selection change.
	static final String PROPERTY_ANIMATED = "animated";
	static final String PROPERTY_ANIMATION_DURATION = "animationDuration";

	private static final String TAG = "TiPillTabs";
	private static final String PROPERTY_ITEMS = "items";
	private static final String PROPERTY_SELECTED_ID = "selectedId";
	private static final String PROPERTY_BADGE = "badge";
	private static final String PROPERTY_BADGE_TINT_COLOR = "badgeTintColor";
	private static final String EVENT_CHANGE = "change";

	private List<PillItem> items = new ArrayList<>();
	private String selectedId;

	public ViewProxy()
	{
		super();
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		PillTabsView view = new PillTabsView(this);
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// The initial configuration never fires `change`, in any key order.
	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);
		if (options.containsKey(PROPERTY_ITEMS)) {
			List<PillItem> parsed = parseItems(options.get(PROPERTY_ITEMS));
			if (parsed != null) {
				items = parsed;
			}
		}
		Object requested = options.get(PROPERTY_SELECTED_ID);
		selectedId = (requested instanceof String && indexOf((String) requested) >= 0)
			? (String) requested : firstId();
	}

	List<PillItem> pillItems()
	{
		return items;
	}

	// Returns copies. A change to a returned dictionary has no effect until JavaScript assigns `items` again.
	@Kroll.getProperty
	public Object[] getItems()
	{
		Object[] result = new Object[items.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new KrollDict(items.get(i).properties);
		}
		return result;
	}

	// An invalid array is rejected as a whole. A valid array keeps the selection when its ID still exists.
	@Kroll.setProperty
	public void setItems(Object value)
	{
		List<PillItem> parsed = parseItems(value);
		if (parsed == null) {
			return;
		}
		String previousId = selectedId;
		items = parsed;
		if (indexOf(selectedId) < 0) {
			selectedId = firstId();
		}
		PillTabsView view = pillView();
		if (view != null) {
			view.rebuild();
		}
		if (!Objects.equals(previousId, selectedId)) {
			fireChange(previousId, "items");
		}
	}

	@Kroll.getProperty
	public String getSelectedId()
	{
		return selectedId;
	}

	// A value that is not a string, for example null, selects the first item.
	@Kroll.setProperty
	public void setSelectedId(Object value)
	{
		select(value instanceof String ? (String) value : null, "programmatic");
	}

	// Fires `change` one time when the selection changes. Ignores unknown IDs and the current ID.
	void select(String requestedId, String reason)
	{
		String nextId = requestedId != null ? requestedId : firstId();
		if (nextId != null && indexOf(nextId) < 0) {
			Log.w(TAG, "Ignoring unknown selectedId: " + requestedId);
			return;
		}
		if (Objects.equals(nextId, selectedId)) {
			return;
		}
		String previousId = selectedId;
		selectedId = nextId;
		PillTabsView view = pillView();
		if (view != null) {
			view.updateSelection();
		}
		fireChange(previousId, reason);
	}

	// The badge methods keep the badge state in `items`, as on iOS.
	// A stock Material chip has no attention dot, so Android does not draw one.
	@Kroll.method
	public void setBadge(@Kroll.argument(optional = true) Object id, @Kroll.argument(optional = true) Object visible)
	{
		updateBadge(id, TiConvert.toBoolean(visible, false));
	}

	@Kroll.method
	public void showBadge(@Kroll.argument(optional = true) Object id)
	{
		updateBadge(id, true);
	}

	@Kroll.method
	public void hideBadge(@Kroll.argument(optional = true) Object id)
	{
		updateBadge(id, false);
	}

	@Kroll.method
	public void setBadgeTintColor(@Kroll.argument(optional = true) Object id,
								  @Kroll.argument(optional = true) Object color)
	{
		PillItem item = find(id);
		if (item == null) {
			return;
		}
		if (color == null) {
			item.properties.remove(PROPERTY_BADGE_TINT_COLOR);
		} else {
			item.properties.put(PROPERTY_BADGE_TINT_COLOR, color);
		}
	}

	private void updateBadge(Object id, boolean visible)
	{
		PillItem item = find(id);
		if (item != null) {
			item.properties.put(PROPERTY_BADGE, visible);
		}
	}

	private PillItem find(Object id)
	{
		int index = id instanceof String ? indexOf((String) id) : -1;
		if (index < 0) {
			Log.w(TAG, "Ignoring badge update for unknown id: " + id);
			return null;
		}
		return items.get(index);
	}

	private List<PillItem> parseItems(Object value)
	{
		if (!(value instanceof Object[])) {
			Log.w(TAG, "items must be an array of dictionaries");
			return null;
		}
		List<PillItem> parsed = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (Object entry : (Object[]) value) {
			PillItem item = PillItem.from(entry);
			if (item == null || !seen.add(item.id)) {
				Log.w(TAG, "Each item needs a unique nonempty id and a title; items update ignored");
				return null;
			}
			parsed.add(item);
		}
		return parsed;
	}

	private int indexOf(String id)
	{
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).id.equals(id)) {
				return i;
			}
		}
		return -1;
	}

	private String firstId()
	{
		return items.isEmpty() ? null : items.get(0).id;
	}

	private PillTabsView pillView()
	{
		TiUIView view = peekView();
		return view instanceof PillTabsView ? (PillTabsView) view : null;
	}

	private void fireChange(String previousId, String reason)
	{
		KrollDict event = new KrollDict();
		event.put("id", selectedId);
		event.put("index", indexOf(selectedId));
		event.put("previousId", previousId);
		event.put("reason", reason);
		fireEvent(EVENT_CHANGE, event);
	}
}
