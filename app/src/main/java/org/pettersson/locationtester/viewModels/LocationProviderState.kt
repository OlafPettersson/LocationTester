package org.pettersson.locationtester.viewModels

enum class LocationProviderState
{
    OFF,           // disabled through config
    NOT_INSTALLED, // Buildin: the location provider is not installed on this system
    NO_PERMISSION, // we are missing the required location permissions
    DISABLED,      // Buildin: system-wide disabled,
    BINDING,       // UnifiedNlp: trying to bind to service
    WAITING,       // waiting to get a location
    CACHED_LOCATION, // Buildin: an old location was found
    POLLED_LOCATION, // UnifiedNlp: The location was polled from the service.
    PUSHED_LOCATION, // Buildin/Unified: the location was pushed to us
}