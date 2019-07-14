/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.events;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.EventListener;
import org.cascadebot.cascadebot.metrics.Metrics;

public class JDAEventMetricsListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        Metrics.INS.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }

}
