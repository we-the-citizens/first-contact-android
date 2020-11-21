# First Contact Android app

![alt text](./FirstContact.png "First Contact Logo")

First Contact was built (initially) to become the Romanian component of the cross-border network of Automatic, Anonymous Contact Tracing applications designed to stop Coronavirus spreading - a network of several compatible national applications, built on top of the Bluetooth Exposure Notification protocol, developed in collaboration by Google and Apple (https://www.google.com/covid19/exposurenotifications/). First Contact fully satisfies the EU regulations contained in the EU toolbox for the use of mobile applications for contact tracing and / or warning (https://ec.europa.eu/health/sites/health/files/ehealth/docs/covid-19_apps_en.pdf).

Our project was split into 3 stages:
- Stage 1: the implementation of a prototype app, based on a heavily modified version of the Bluetrace protocol (https://bluetrace.io/), that has been changed to follow a fully descentralized, privacy-centric model, identical with the Exposure Notifications architecture. 
- Stage 2: public release of the prototype in order to demostrate the viability of the concept and persuade authorities and the general public that Romania should adop a contact tracing solution based on Exposure Notifications API and that First Contact could easily become the official Contact Tracing application of Romania.
- Stage 3: after receiving approval from the authorities, we replace the core engine of the app, from the current Bluetrace BLE detection to the Exposure Notification API and finalize the final, official COVID-19 Contact Tracing application of Romania.

<b>Because of the reluctance of the authorities to support this project, we modified the application in order to be able to fulfill its role (to warn users in case of an exposure to Coronavirus) even without the cooperation of the authorities.</b>

Full client sources are released under GPL3 license (https://github.com/we-the-citizens/first-contact-android/blob/final/LICENSE.md), as the original OpenTrace (https://github.com/opentrace-community/opentrace-android) codebase. We invite everybody to review our code and make comments, suggestions or contributions. 

Visit https://first-contact.ro to learn more.
