# OpenAir

OpenAir is a privacy focused fitness tracker. Your data is yours, and we think it should stay that way.


## Installation

1. Clone our repo
2. Optionally [get a maps API key](https://developers.google.com/maps/documentation/android-sdk/start#step_4_set_up_a_google_maps_api_key) (you can skip to step 4 if you don't wish to use google maps)
3. Create a file in the git root named `local.properties` and add the maps API key `MAPS_API_KEY=XXXXX`
4. Connect your device with usb
5. Build and install the apk `./gradlew installDebug`


## Contributing
[See our contributing guidlines](CONTRIBUTING.md)

In addition to the guidelines above, you may want to generate and add a maps API key as specified in the installation instructions, depending on whether your changes depend on maps.


## License
[GPLv3](LICENSE)
