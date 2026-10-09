// TiPillTabs — UIKit implementation of the MTabBar interaction.
// Copyright (c) 2026 DesignByMind LLC. MIT license.
// Based on MTabBar by Balaji Venkatesh (Kavsoft), MIT.

import TitaniumKit
import UIKit

private struct PillItem {
  let id: String
  let title: String
  let systemImage: String
  let tintColor: UIColor
  let backgroundColor: UIColor
  let activeTintColor: UIColor
  let activeBackgroundColor: UIColor
  var badge: Bool
  var badgeTintColor: UIColor?
}

private final class PillButton: UIControl {
  let icon = UIImageView()
  let titleLabel = UILabel()
  let badgeDot = UIView()

  override init(frame: CGRect) {
    super.init(frame: frame)
    clipsToBounds = true
    layer.cornerCurve = .continuous
    icon.contentMode = .scaleAspectFit
    icon.isUserInteractionEnabled = false
    titleLabel.isUserInteractionEnabled = false
    titleLabel.lineBreakMode = .byTruncatingTail
    addSubview(icon)
    addSubview(titleLabel)
    badgeDot.isUserInteractionEnabled = false
    badgeDot.isAccessibilityElement = false
    badgeDot.isHidden = true
    addSubview(badgeDot)
    isAccessibilityElement = true
  }

  required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }
}

// All geometry is local to this view. No parent table/scroll delegates are replaced.
private final class PillTabsControl: UIView, UIGestureRecognizerDelegate {
  var items: [PillItem] = []
  var selectedId: String?
  var aggregateId: String?
  var spacing: CGFloat = 8
  var trailingVisibility: CGFloat = 5
  var rightPadding: CGFloat = 0
  var animationDuration: TimeInterval = 0.3
  var animated = true
  var toggleOnReselect = true
  var onChange: ((String?, String?, String) -> Void)?
  var gestureEnabled = false { didSet { pan.isEnabled = gestureEnabled } }

  private var previousCategoryId: String?
  private var buttons: [PillButton] = []
  private let pillsContainer = UIView(frame: .zero)
  private var animator: UIViewPropertyAnimator?
  private var lastSize = CGSize.zero
  private lazy var pan = UIPanGestureRecognizer(target: self, action: #selector(panned(_:)))
  private var titleFont: UIFont {
    let font = UIFont.preferredFont(forTextStyle: .callout)
    return UIFont.systemFont(ofSize: font.pointSize, weight: .semibold)
  }

  var effectiveAggregateId: String? {
    if let aggregateId = aggregateId {
      return items.contains(where: { $0.id == aggregateId }) ? aggregateId : nil
    }
    return items.last?.id
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
    clipsToBounds = true
    pillsContainer.clipsToBounds = true
    addSubview(pillsContainer)
    pan.delegate = self
    pan.isEnabled = false
    addGestureRecognizer(pan)
    registerForTraitChanges([UITraitPreferredContentSizeCategory.self, UITraitUserInterfaceStyle.self, UITraitLayoutDirection.self]) {
      (view: PillTabsControl, _: UITraitCollection) in view.render(animated: false)
    }
  }

  required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

  func replaceItems(_ newItems: [PillItem], notify: Bool) {
    stopAnimations()
    let oldId = selectedId
    items = newItems
    if !items.contains(where: { $0.id == selectedId }) { selectedId = items.first?.id }
    if !items.contains(where: { $0.id == previousCategoryId }) { previousCategoryId = nil }
    if selectedId != effectiveAggregateId { previousCategoryId = selectedId }
    buttons.forEach { $0.removeFromSuperview() }
    buttons = items.enumerated().map { index, item in
      let button = PillButton(frame: .zero)
      button.tag = index
      button.icon.image = UIImage(systemName: item.systemImage) ?? UIImage(systemName: "questionmark")
      button.titleLabel.text = item.title
      button.accessibilityLabel = item.title
      button.addTarget(self, action: #selector(tapped(_:)), for: .touchUpInside)
      pillsContainer.addSubview(button)
      return button
    }
    render(animated: false)
    if notify && oldId != selectedId { onChange?(oldId, selectedId, "items") }
  }

  func select(_ requestedId: String?, reason: String, notify: Bool, animate: Bool) {
    let nextId = requestedId ?? items.first?.id
    guard nextId == nil || items.contains(where: { $0.id == nextId }) else {
      NSLog("[TiPillTabs] Ignoring unknown selectedId: %@", requestedId ?? "")
      return
    }
    let oldId = selectedId
    selectedId = nextId
    if nextId != effectiveAggregateId { previousCategoryId = nextId }
    render(animated: animate && oldId != nextId)
    if notify && oldId != nextId { onChange?(oldId, nextId, reason) }
  }

  func finishConfiguration() {
    if !items.contains(where: { $0.id == selectedId }) { selectedId = items.first?.id }
    if selectedId != effectiveAggregateId { previousCategoryId = selectedId }
    if previousCategoryId == nil { previousCategoryId = items.first(where: { $0.id != effectiveAggregateId })?.id }
    render(animated: false)
  }

  func updateBadge(id: String, visible: Bool?, tintColor: UIColor?, updateTint: Bool) {
    guard let index = items.firstIndex(where: { $0.id == id }) else { return }
    if let visible = visible { items[index].badge = visible }
    if updateTint { items[index].badgeTintColor = tintColor }
    // A badge-only update must not stop or restart the selection animation.
    updateBadgeAppearance(at: index)
  }

  private func updateBadgeAppearance(at index: Int) {
    let item = items[index]
    let button = buttons[index]
    let active = item.id == selectedId
    button.badgeDot.isHidden = !item.badge || active
    button.badgeDot.backgroundColor = item.badgeTintColor ?? item.tintColor
    // The pill-colored ring separates the dot from the symbol, as in Mail.
    button.badgeDot.layer.borderColor = (active ? item.activeBackgroundColor : item.backgroundColor)
      .resolvedColor(with: traitCollection).cgColor
    button.accessibilityValue = item.badge ? "Needs attention" : nil
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    if lastSize != bounds.size {
      lastSize = bounds.size
      render(animated: false)
    }
  }

  override func didMoveToWindow() {
    super.didMoveToWindow()
    if window == nil { stopAnimations() }
  }

  func stopAnimations() {
    if let animator = animator, animator.state == .active {
      animator.stopAnimation(false)
      animator.finishAnimation(at: .current)
    }
    animator = nil
    buttons.forEach {
      $0.layer.removeAllAnimations()
      $0.icon.layer.removeAllAnimations()
      $0.titleLabel.layer.removeAllAnimations()
    }
  }

  func render(animated requestedAnimation: Bool) {
    stopAnimations()
    // Padding is physical right space inside the Titanium view. The inner
    // viewport clips only the intentional aggregate overflow, before that gap.
    let contentWidth = max(0, bounds.width - rightPadding)
    pillsContainer.frame = CGRect(x: 0, y: 0, width: contentWidth, height: bounds.height)
    pillsContainer.isHidden = contentWidth <= 0 || bounds.height <= 0
    guard !items.isEmpty, contentWidth > 0, bounds.height > 0 else { return }
    let selectedIndex = items.firstIndex(where: { $0.id == selectedId }) ?? 0
    let font = titleFont
    let titleWidth = ceil((items[selectedIndex].title as NSString).size(withAttributes: [.font: font]).width)
    let count = items.count
    let gap = min(spacing, contentWidth / CGFloat(max(1, count * 2)))
    let available = max(0, contentWidth - CGFloat(count - 1) * gap)
    // Keep the aggregate pill just inside the trailing edge in category mode.
    // Fall back to fitting every pill when the view is too narrow for this layout.
    let wantsPeek = count >= 3 && effectiveAggregateId == items.last?.id && selectedId != effectiveAggregateId
    let peek = min(trailingVisibility, contentWidth * 0.1)
    let minInactive: CGFloat = 28
    let canPeek = wantsPeek && available - peek >= titleWidth + 66 + CGFloat(count - 2) * minInactive
    let slots = max(1, count - (canPeek ? 2 : 1))
    let activeWidth = count == 1 ? contentWidth : min(available, min(titleWidth + 66, max(20, available - CGFloat(slots) * minInactive - (canPeek ? peek : 0))))
    let inactiveWidth = max(0, (available - activeWidth - (canPeek ? peek : 0)) / CGFloat(slots))
    let duration = animationDuration
    let shouldAnimate = requestedAnimation && animated && duration > 0 && window != nil && !UIAccessibility.isReduceMotionEnabled

    let geometry = { [self] in
      let rtl = effectiveUserInterfaceLayoutDirection == .rightToLeft
      var x: CGFloat = 0
      for (index, button) in buttons.enumerated() {
        let item = items[index]
        let active = index == selectedIndex
        let width = active ? activeWidth : inactiveWidth
        button.frame = CGRect(x: rtl ? contentWidth - x - width : x, y: 0, width: width, height: bounds.height)
        button.layer.cornerRadius = bounds.height / 2
        let iconWidth = min(20, max(0, width))
        let iconX = active ? min(20, max(0, (width - iconWidth) / 2)) : (width - iconWidth) / 2
        button.icon.frame = CGRect(x: rtl ? width - iconX - iconWidth : iconX, y: (bounds.height - 20) / 2, width: iconWidth, height: 20)
        // Six-point dot with a 1.5-point knockout ring, at the icon's upper right.
        button.badgeDot.frame = CGRect(x: button.icon.frame.maxX - 6,
                                      y: button.icon.frame.minY - 2, width: 9, height: 9)
        button.badgeDot.layer.cornerRadius = 4.5
        button.badgeDot.layer.borderWidth = 1.5
        button.icon.preferredSymbolConfiguration = UIImage.SymbolConfiguration(pointSize: 17, weight: .regular)
        button.titleLabel.font = font
        button.titleLabel.frame = CGRect(x: rtl ? 20 : iconX + 26, y: 0, width: max(0, width - iconX - 46), height: bounds.height)
        button.titleLabel.textAlignment = rtl ? .right : .left
        button.backgroundColor = active ? item.activeBackgroundColor : item.backgroundColor
        button.icon.tintColor = active ? item.activeTintColor : item.tintColor
        button.titleLabel.textColor = active ? item.activeTintColor : item.tintColor
        updateBadgeAppearance(at: index)
        button.accessibilityTraits = active ? [.button, .selected] : [.button]
        button.accessibilityHint = toggleOnReselect && active && effectiveAggregateId != nil
          ? "Double tap to switch between this category and all items." : nil
        x += width + gap
      }
      // Hide fully clipped pills from VoiceOver; expose them when selected.
      for button in buttons { button.accessibilityElementsHidden = !pillsContainer.bounds.intersects(button.frame) }
    }

    if shouldAnimate {
      let nextAnimator = UIViewPropertyAnimator(duration: duration, dampingRatio: 1, animations: geometry)
      animator = nextAnimator
      nextAnimator.startAnimation()
    } else {
      UIView.performWithoutAnimation(geometry)
    }
    for (index, button) in buttons.enumerated() {
      let active = index == selectedIndex
      let fade = { button.titleLabel.alpha = active ? 1 : 0 }
      if shouldAnimate {
        UIView.animate(withDuration: active ? duration : duration / 2.5, delay: 0,
                       options: [.beginFromCurrentState, .allowUserInteraction], animations: fade)
      } else {
        UIView.performWithoutAnimation(fade)
      }
    }
  }

  @objc private func tapped(_ button: PillButton) {
    guard items.indices.contains(button.tag) else { return }
    let id = items[button.tag].id
    if toggleOnReselect && id == selectedId, let aggregate = effectiveAggregateId {
      let target = id == aggregate ? previousCategoryId : aggregate
      if let target = target { select(target, reason: "tap", notify: true, animate: true) }
    } else {
      select(id, reason: "tap", notify: true, animate: true)
    }
  }

  override func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
    let velocity = pan.velocity(in: self)
    return items.count > 1 && abs(velocity.x) > abs(velocity.y)
  }

  @objc private func panned(_ recognizer: UIPanGestureRecognizer) {
    guard recognizer.state == .ended else { return }
    let translation = recognizer.translation(in: self)
    guard abs(translation.x) > 40, abs(translation.x) > abs(translation.y) else { return }
    let rtl = effectiveUserInterfaceLayoutDirection == .rightToLeft
    let towardAggregate = rtl ? translation.x > 0 : translation.x < 0
    if let target = towardAggregate ? effectiveAggregateId : previousCategoryId {
      select(target, reason: "swipe", notify: true, animate: true)
    }
  }
}

@objc(TiPilltabsViewProxy)
class TiPilltabsViewProxy: TiViewProxy {
  override func newView() -> TiUIView! { TiPilltabsView(frame: .zero) }

  @objc(setBadge:)
  func setBadge(_ arguments: [Any]?) {
    guard let arguments = arguments, arguments.count >= 2,
          let id = arguments[0] as? String else { return }
    updateBadge(id: id, visible: TiUtils.boolValue(arguments[1], def: false), tint: nil, updateTint: false)
  }

  @objc(showBadge:)
  func showBadge(_ arguments: [Any]?) {
    guard let id = arguments?.first as? String else { return }
    updateBadge(id: id, visible: true, tint: nil, updateTint: false)
  }

  @objc(hideBadge:)
  func hideBadge(_ arguments: [Any]?) {
    guard let id = arguments?.first as? String else { return }
    updateBadge(id: id, visible: false, tint: nil, updateTint: false)
  }

  @objc(setBadgeTintColor:)
  func setBadgeTintColor(_ arguments: [Any]?) {
    guard let arguments = arguments, arguments.count >= 2,
          let id = arguments[0] as? String else { return }
    updateBadge(id: id, visible: nil, tint: arguments[1], updateTint: true)
  }

  private func updateBadge(id: String, visible: Bool?, tint: Any?, updateTint: Bool) {
    TiThreadPerformOnMainThread({
      (self.view as? TiPilltabsView)?.updateBadge(id: id, visible: visible, tint: tint, updateTint: updateTint)
    }, true)
  }

  override func windowDidClose() {
    if viewAttached(), let pillView = view as? TiPilltabsView { pillView.stopAnimations() }
    super.windowDidClose()
  }
}

@objc(TiPilltabsView)
class TiPilltabsView: TiUIView {
  private let control = PillTabsControl(frame: .zero)
  private var configured = false
  private var pendingSelectedId: String?
  private var acceptedItems: [[String: Any]] = []

  override func initializeState() {
    super.initializeState()
    addSubview(control)
    control.onChange = { [weak self] previousId, id, reason in
      guard let self = self else { return }
      self.syncSelection()
      self.proxy.fireEvent("change", with: [
        "id": id as Any? ?? NSNull(),
        "index": self.control.items.firstIndex(where: { $0.id == id }) ?? -1,
        "previousId": previousId as Any? ?? NSNull(),
        "reason": reason
      ])
    }
  }

  override func configurationSet() {
    super.configurationSet()
    guard !configured else {
      control.render(animated: false)
      return
    }
    control.selectedId = pendingSelectedId
    control.finishConfiguration()
    configured = true
    syncSelection()
  }

  override func frameSizeChanged(_ frame: CGRect, bounds: CGRect) {
    super.frameSizeChanged(frame, bounds: bounds)
    control.frame = CGRect(origin: .zero, size: bounds.size)
    control.setNeedsLayout()
  }

  func stopAnimations() { control.stopAnimations() }

  func updateBadge(id: String, visible: Bool?, tint: Any?, updateTint: Bool) {
    guard let index = acceptedItems.firstIndex(where: { $0["id"] as? String == id }) else {
      NSLog("[TiPillTabs] Ignoring badge update for unknown id: %@", id)
      return
    }
    if let visible = visible { acceptedItems[index]["badge"] = visible }
    if updateTint {
      if tint == nil || tint is NSNull { acceptedItems[index].removeValue(forKey: "badgeTintColor") }
      else { acceptedItems[index]["badgeTintColor"] = tint }
    }
    control.updateBadge(id: id, visible: visible, tintColor: TiUtils.colorValue(tint)?.color, updateTint: updateTint)
    proxy.replaceValue(acceptedItems, forKey: "items", notification: false)
  }

  private func syncSelection() {
    proxy.replaceValue(control.selectedId as Any? ?? NSNull(), forKey: "selectedId", notification: false)
  }

  @objc(setItems_:)
  func setItems_(_ value: Any?) {
    guard let values = value as? [[String: Any]] else {
      NSLog("[TiPillTabs] items must be an array of dictionaries")
      proxy.replaceValue(acceptedItems, forKey: "items", notification: false)
      return
    }
    var seen = Set<String>()
    var parsed: [PillItem] = []
    for dictionary in values {
      guard let id = dictionary["id"] as? String, !id.isEmpty, seen.insert(id).inserted,
            let title = dictionary["title"] as? String,
            let image = dictionary["systemImage"] as? String else {
        NSLog("[TiPillTabs] Each item needs a unique nonempty id, title and systemImage; items update ignored")
        proxy.replaceValue(acceptedItems, forKey: "items", notification: false)
        return
      }
      parsed.append(PillItem(
        id: id, title: title, systemImage: image,
        tintColor: TiUtils.colorValue(dictionary["tintColor"])?.color ?? .secondaryLabel,
        backgroundColor: TiUtils.colorValue(dictionary["backgroundColor"])?.color ?? .tertiarySystemFill,
        activeTintColor: TiUtils.colorValue(dictionary["activeTintColor"])?.color ?? .white,
        activeBackgroundColor: TiUtils.colorValue(dictionary["activeBackgroundColor"])?.color ?? .systemBlue,
        badge: TiUtils.boolValue(dictionary["badge"], def: false),
        badgeTintColor: TiUtils.colorValue(dictionary["badgeTintColor"])?.color
      ))
    }
    acceptedItems = values
    control.replaceItems(parsed, notify: configured)
    if configured { syncSelection() }
  }

  @objc(setSelectedId_:)
  func setSelectedId_(_ value: Any?) {
    pendingSelectedId = value as? String
    if configured {
      control.select(pendingSelectedId, reason: "programmatic", notify: true, animate: true)
      syncSelection()
    }
  }

  @objc(setAggregateId_:)
  func setAggregateId_(_ value: Any?) {
    control.aggregateId = value as? String
    control.finishConfiguration()
  }

  @objc(setSpacing_:)
  func setSpacing_(_ value: Any?) {
    control.spacing = finiteNumber(value, fallback: 8)
    control.render(animated: false)
  }

  @objc(setTrailingVisibility_:)
  func setTrailingVisibility_(_ value: Any?) {
    control.trailingVisibility = finiteNumber(value, fallback: 5)
    control.render(animated: false)
  }

  @objc(setRightPadding_:)
  func setRightPadding_(_ value: Any?) {
    control.rightPadding = finiteNumber(value, fallback: 0)
    proxy.replaceValue(control.rightPadding, forKey: "rightPadding", notification: false)
    control.render(animated: false)
  }

  @objc(setGestureEnabled_:)
  func setGestureEnabled_(_ value: Any?) { control.gestureEnabled = TiUtils.boolValue(value, def: false) }

  @objc(setToggleOnReselect_:)
  func setToggleOnReselect_(_ value: Any?) { control.toggleOnReselect = TiUtils.boolValue(value, def: true) }

  @objc(setAnimated_:)
  func setAnimated_(_ value: Any?) { control.animated = TiUtils.boolValue(value, def: true) }

  @objc(setAnimationDuration_:)
  func setAnimationDuration_(_ value: Any?) {
    control.animationDuration = TimeInterval(finiteNumber(value, fallback: 300)) / 1000
  }

  private func finiteNumber(_ value: Any?, fallback: Double) -> CGFloat {
    let number = TiUtils.doubleValue(value, def: fallback)
    return CGFloat(number.isFinite ? max(0, number) : fallback)
  }
}
