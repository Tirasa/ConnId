# What is ConnId?
ConnId (Connectors for Identity Management) is built to help drive development of Connectors.

Connectors provide a consistent generic layer between applications and target resources.
The main focus of the API is provisioning operations and password management. The toolkit is intended to facilitate development with as few dependences as possible.

# Why ConnId?
ConnId's main purpose is to build a new home for the [Identity Connectors project](http://java.net/projects/identityconnectors/), 
with all that is required nowadays for a modern Open Source project: Apache Maven driven build, artifacts and 
mailing lists.

# Is this Open Source?
All the code and documentation are released under terms of 
[CCDL 1.0](https://raw.github.com/Tirasa/ConnId/master/legal/license.txt).

# What is ConnId meant for?
One of the main features of the framework is decoupling Connectors from any application that uses them. 
This means each Connector implementation can be easily replaced and not necessarily depend on a specific version 
of the framework. In addition an application may choose to use multiple Connectors which can require class path 
isolation. With class path isolation there is no issue with conflicting 3rd party libraries.
An application is only required to couple to the Framework and not to any specific Connector.

![ConnId architecture](https://raw.github.com/Tirasa/ConnId/master/images/architecture.png "ConnId architecture")
