//
//  TiPilltabsModule.swift
//  TiPillTabs
//
//  Created by DesignByMind LLC
//  Copyright (c) 2026 DesignByMind LLC. All rights reserved.
//

import TitaniumKit

@objc(TiPilltabsModule)
class TiPilltabsModule: TiModule {

  func moduleGUID() -> String {
    return "f62783ea-53d4-4248-832e-c4a3a2ef675a"
  }

  override func moduleId() -> String! {
    return "ti.pilltabs"
  }

}
