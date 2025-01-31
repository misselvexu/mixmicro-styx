/*
  Copyright (C) 2013-2022 Expedia Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.hotels.styx.client.connectionpool;

import com.google.common.base.Ticker;
import com.hotels.styx.api.Clocks;
import com.hotels.styx.api.extension.Origin;
import com.hotels.styx.client.Connection;
import com.hotels.styx.client.ConnectionSettings;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

import static com.hotels.styx.api.Clocks.systemClock;
import static java.util.Objects.requireNonNull;

/**
 * Decorator for a connection factory that wraps newly created connections into a decorator that tracks and expires the connection
 * on expiration time.
 */
public class ExpiringConnectionFactory implements Connection.Factory {
    private final long connectionExpirationSeconds;
    private final Connection.Factory connectionFactory;

    public ExpiringConnectionFactory(long connectionExpirationSeconds, Connection.Factory connectionFactory) {
        this.connectionExpirationSeconds = connectionExpirationSeconds;
        this.connectionFactory = requireNonNull(connectionFactory);
    }

    @Override
    public Mono<Connection> createConnection(Origin origin, ConnectionSettings connectionSettings) {
        return connectionFactory
                .createConnection(origin, connectionSettings)
                .map(this::decorate);
    }

    private Connection decorate(Connection conn) {
        return new ExpiringConnection(conn, connectionExpirationSeconds, systemClock());
    }
}
