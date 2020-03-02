/*
 * Copyright (c) 2020 Payara Foundation and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package fish.payara.server.config;

import com.intellij.javaee.oss.util.CachedConfig;
import fish.payara.server.PayaraLocalModel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class PayaraConfig extends CachedConfig<PayaraLocalModel> {

    private final PayaraLocalModel model;

    protected PayaraConfig(PayaraLocalModel model) {
        this.model = model;
    }

    public File getDomainConfig() {
        return model.getDomainConfig();
    }

    @Override
    protected long getStamp(PayaraLocalModel model) {
        return CachedConfig.getStamp(model.getDomainConfig());
    }

    protected abstract static class PayaraConfigFactory<T extends PayaraConfig>
            implements Factory<PayaraLocalModel, T> {

        private final Map<Key, T> cache = new HashMap<>();

        @NotNull
        @Override
        public Key createKey(PayaraLocalModel model) {
            return new Key(new String[]{model.getHome(), model.DOMAIN_NAME});
        }

        public T get(PayaraLocalModel model) {
            return (T) PayaraConfig.get(this.cache, this, model);
        }

        @NotNull
        @Override
        public abstract T createConfig(PayaraLocalModel model);

    }

}
