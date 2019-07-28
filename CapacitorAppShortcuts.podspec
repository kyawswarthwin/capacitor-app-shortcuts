
  Pod::Spec.new do |s|
    s.name = 'CapacitorAppShortcuts'
    s.version = '0.0.1'
    s.summary = 'App shortcuts capacitor plugin'
    s.license = 'MIT'
    s.homepage = 'a'
    s.author = 'Tobias Staudt'
    s.source = { :git => 'a', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end