resources

Provide an extremely lightweight REST endpoint manager.

Quick Start:

Define four classes:
-MyContainer extends Container
-MyItem extends Item
-MyModule extends Module
-MyResource extends Resource


Resource follows jersey protocols.
Container serves as the endpoint to each resource.
Item serves as the underlying object.
Module is used to connect all the pieces together
