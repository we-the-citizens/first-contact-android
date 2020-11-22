# First Contact Android app

![alt text](./FirstContact.png "First Contact Logo")

First Contact was build to become Romania's privacy-centric Contact Tracing application, designed to limit the spread of the Coronavirus. This application satisfies (and sometimes exceeds) the privacy recommendations for the mobile applications to support contact tracing, established in the <a href="https://ec.europa.eu/health/sites/health/files/ehealth/docs/covid-19_apps_en.pdf">Common EU Toolbox for Member States</a>.

In our development strategy, we splitted the project in 3 stages:
- <b>Stage 1:</b> The development of a prototype app, based on a heavily modified version of the <a href="https://bluetrace.io/Bluetrace">BlueTrace protocol</a>, that has been changed to follow a fully descentralized, privacy-centric model, similar to the <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api">Google&Apple's Exposure Notifications architecture</a>. 
- <b>Stage 2:</b> Public release of a prototype version, in order to demostrate the viability of the concept and to persuade authorities and the general public that Romania should adopt a privacy-centric, decentralised, Bluetooth based contact tracing solution.
- <del>Stage 3 - Plan A: In case of approval by the authorities, we intended to replace the core engine of the app, from the current BlueTrace BLE detection to the Google&Apple's Exposure Notification API.</del>
- <b>Stage 3 - Plan B:</b> Adjusting the application's architecture, so it can fulfill its intended role (to warn users in case of an exposure to Coronavirus), independent of the romanian authorities involvement.

<b>Because of the authorities reluctance in supporting this project, we've been left with no other choice but to go ahead with the Plan B, in the final stage of this project.</b>

The client sources are released under <a href="https://github.com/we-the-citizens/first-contact-android/blob/final/LICENSE.md">GPL3 license</a>, as the original <a href="https://github.com/opentrace-community/opentrace-android">OpenTrace codebase</a>. We invite all those interested to review our code and make comments, suggestions or contributions. 

Visit https://first-contact.ro to learn more.
