-- Add the default observers.
-- This must run after the default_admin script.
-- Define the observer's IDs.
SET
    @ohmage.admin_id = (SELECT id FROM user WHERE username='ohmage.admin'),
    @analytics_observer_id = 'org.ohmage.Analytics',
    @logprobe_observer_id = 'org.ohmage.LogProbe',
    @mobility_observer_id = 'edu.ucla.cens.Mobility';

-- Insert the observer definitions.
INSERT INTO 
    observer(user_id, observer_id, version, name, description, version_string)
    VALUES
        (
            @ohmage.admin_id,
            @analytics_observer_id,
            1,
            'Ohmage Analytics',
            'Logs analytics specific to ohmage.',
            '1.0'
        ),
        (
            @ohmage.admin_id,
            @logprobe_observer_id,
            1,
            'LogProbe',
            'Logs data and analytics.',
            '1.0'
        ),
        (
            @ohmage.admin_id,
            @mobility_observer_id,
            2012061300,
            'Mobility',
            'Collects movement information about a user, e.g. walk, run, etc.',
            '3.0'
        );
        
-- Get the database IDs for the observers.
SET
    @analytics_id =
        (SELECT id FROM observer WHERE observer_id = @analytics_observer_id),
    @logprobe_id =
        (SELECT id FROM observer WHERE observer_id = @logprobe_observer_id),
    @mobility_id = 
        (SELECT id FROM observer WHERE observer_id = @mobility_observer_id);

-- Define the stream IDs
SET
    @analytics_prompt_stream_id = 'prompt',
    @analytics_trigger_stream_id = 'trigger',
    @logprobe_activity_stream_id = 'activity',
    @logprobe_log_stream_id = 'log',
    @logprobe_network_stream_id = 'network',
    @logprobe_service_stream_id = 'service',
    @logprobe_widget_stream_id = 'widget',
    @mobility_regular_stream_id = 'regular',
    @mobility_error_stream_id = 'error',
    @mobility_extended_stream_id = 'extended';

-- Insert the stream definitions.
INSERT INTO
    observer_stream(
        stream_id,
        version,
        name,
        description,
        with_id,
        with_timestamp,
        with_location,
        stream_schema)
    VALUES
        (
            @analytics_prompt_stream_id,
            1,
            'Prompt',
            'Logs information about what prompts are shown.',
            true,
            true,
            false,
            '{"type":"object","doc":"Prompt interaction.","schema":[{"name":"id","doc":"The promptId.","type":"string"},{"name":"type","doc":"The prompt type.","type":"string"},{"name":"status","doc":"Status information about the activity. Either ON or OFF.","type":"string"}]}'
        ),
        (
            @analytics_trigger_stream_id,
            1,
            'Trigger',
            'Logs information about triggers in ohmage.',
            true,
            true,
            false,
            '{"type":"object","doc":"Trigger interaction.","schema":[{"name":"action","doc":"The action that was currently applied to the trigger. One of delete, add, trigger, update, or ignore.","type":"string"},{"name":"type","doc":"Trigger type.","type":"string"},{"name":"count","doc":"Current number of triggers for this campaign and trigger type set up in the system.","type":"number"},{"name":"campaign","doc":"The campaign urn to which this trigger belongs.","type":"string"}]}'
        ),
        (
            @logprobe_activity_stream_id,
            1,
            'Activity',
            'Analytics for activity interaction.',
            true,
            true,
            false,
            '{"type":"object","doc":"Activity interaction.","schema":[{"name":"activity","doc":"The class name of the activity.","type":"string"},{"name":"status","doc":"Status information about the activity. Either ON or OFF.","type":"string"}]}'
        ),
        (
            @logprobe_log_stream_id,
            1,
            'Log',
            'Log message.',
            true,
            true,
            false,
            '{"type":"object","doc":"log message.","schema":[{"name":"level","doc":"Log level. One of error, warning, info, debug, or verbose.","type":"string"},{"name":"tag","doc":"Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.","type":"string"},{"name":"message","doc":"The message you would like logged.","type":"string"}]}'
        ),
        (
            @logprobe_network_stream_id,
            1,
            'Network',
            'Analytics for network.',
            true,
            true,
            false,
            '{"type":"object","doc":"Network information.","schema":[{"name":"context","doc":"Class name of the context which started the call to the network.","type":"string"},{"name":"resource","doc":"Name of the resource requested","type":"string"},{"name":"network_state","doc":"upload or download","type":"string"},{"name":"length","doc":"Number of bytes transmitted","type":"number"}]}'
        ),
        (
            @logprobe_service_stream_id,
            1,
            'Service',
            'Analytics for service interaction.',
            true,
            true,
            false,
            '{"type":"object","doc":"Service interaction.","schema":[{"name":"service","doc":"The class name of the service.","type":"string"},{"name":"status","doc":"Status information about the service. Either ON or OFF.","type":"string"}]}'
        ),
        (
            @logprobe_widget_stream_id,
            1,
            'Widget',
            'Analytics for widget interaction.',
            true,
            true,
            false,
            '{"type":"object","doc":"Widget interaction.","schema":[{"name":"id","doc":"The id of the view which produced this event.","type":"number"},{"name":"name","doc":"The human readable name of the view which produced this event. Will read the getContentDescription() of the view.","type":"string"},{"name":"extra","doc":"Extra information supplied for this interaction.","optional":true,"type":"string"}]}'
        ),
        (
            @mobility_regular_stream_id,
            2012050700,
            'Mobility - Regular',
            'This records only the user\'s mode.',
            true,
            true,
            NULL,
            '{"type":"object","doc":"Only requires the user\'s mode.","schema":[{"name":"mode","type":"string","doc":"The user\'s mode."}]}'
        ),
        (
            @mobility_error_stream_id,
            2012061300,
            'Mobility - Error',
            'This is used to signify an error with a point. The mode is the only thing that is required, which must be null.',
            true,
            true,
            NULL,
            '{"type":"object","doc":"Only requires the user\'s mode, which must be \\"error\\".","schema":[{"name":"mode","type":"string","doc":"The user\'s mode."}]}'
        ),
        (
            @mobility_extended_stream_id,
            2012050700,
            'Mobility - Extended',
            'This records the user\'s mode as well as accelerometer, WiFi, and GPS data.',
            true,
            true,
            NULL,
            '{"type":"object","doc":"Contains the user\'s mode as well as the sensor data.","schema":[{"name":"mode","type":"string","doc":"The user\'s mode."},{"name":"speed","type":"number","doc":"The user\'s speed in the last minute.","optional":true},{"name":"accel_data","type":"array","schema":{"type":"object","doc":"A single accelerometer reading.","schema":[{"name":"x","type":"number","doc":"The x-component of the accelerometer reading."},{"name":"y","type":"number","doc":"The y-component of the accelerometer reading."},{"name":"z","type":"number","doc":"The z-component of the accelerometer reading."}]},"doc":"An array of the accelerometer readings over the last minute."},{"name":"wifi_data","type":"object","doc":"A WiFi reading from the last minute.","schema":[{"name":"time","type":"number","doc":"The time this data was recorded."},{"name":"timezone","type":"string","doc":"The time zone of the device when this data was recorded."},{"name":"scan","type":"array","schema":{"type":"object","doc":"A single access point\'s information.","schema":[{"name":"ssid","type":"string","doc":"The access point\'s SSID."},{"name":"strength","type":"number","doc":"The strength of the signal from the access point."}]},"doc":"The scan of WiFi information."}]}]}'
        );

-- Get the database IDs for the streams.
SET
    @analytics_prompt_id = 
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @analytics_prompt_stream_id
        ),
    @analytics_trigger_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @analytics_trigger_stream_id
        ),
    @logprobe_activity_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @logprobe_activity_stream_id
        ),
    @logprobe_log_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @logprobe_log_stream_id
        ),
    @logprobe_network_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @logprobe_network_stream_id
        ),
    @logprobe_service_stream_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @logprobe_service_stream_id
        ),
    @logprobe_widget_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @logprobe_widget_stream_id
        ),
    @mobility_regular_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @mobility_regular_stream_id
        ),
    @mobility_error_id =  
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @mobility_error_stream_id
        ),
    @mobility_extended_id = 
        (
            SELECT id 
            FROM observer_stream 
            WHERE stream_id = @mobility_extended_stream_id
        );

-- Connect the observers to their respective streams.
INSERT INTO
    observer_stream_link(observer_id, observer_stream_id)
    VALUES
        (@analytics_id, @analytics_prompt_id),
        (@analytics_id, @analytics_trigger_id),
        (@logprobe_id, @logprobe_activity_id),
        (@logprobe_id, @logprobe_log_id),
        (@logprobe_id, @logprobe_network_id),
        (@logprobe_id, @logprobe_service_stream_id),
        (@logprobe_id, @logprobe_widget_id),
        (@mobility_id, @mobility_regular_id),
        (@mobility_id, @mobility_error_id),
        (@mobility_id, @mobility_extended_id);