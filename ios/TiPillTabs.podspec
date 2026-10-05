
Pod::Spec.new do |s|

    s.name         = "TiPillTabs"
    s.version      = "1.0.0"
    s.summary      = "The TiPillTabs Titanium module."

    s.description  = <<-DESC
                     The TiPillTabs Titanium module.
                     DESC

   s.homepage     = "https://github.com/designbymind/TiPillTabs"
    s.license      = { :type => "MIT", :file => "../LICENSE" }
    s.author       = 'DesignByMind LLC'

    s.platform     = :ios
    s.ios.deployment_target = '17.0'

    s.source       = { :git => "https://github.com/designbymind/TiPillTabs.git", :tag => "iOS-v#{s.version}" }

    s.ios.weak_frameworks = 'UIKit', 'Foundation'

    s.ios.dependency 'TitaniumKit'

    s.public_header_files = 'Classes/*.h'
    s.source_files = 'Classes/*.{h,m,swift}'
  end
