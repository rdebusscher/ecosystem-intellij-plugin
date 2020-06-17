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
package fish.payara.micro.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.util.Collections.unmodifiableList;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class VersionRepository {

    private static VersionRepository versionRepository;
    private static final List<MicroVersion> MICRO_VERSIONS = new ArrayList<>();

    private VersionRepository() {
        MICRO_VERSIONS.add(new MicroVersion("5.201", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.194", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.193", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.192", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.191", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.184", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.183", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.182", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.181", "8.0"));
    }

    public static VersionRepository getInstance() {
        if (versionRepository == null) {
            versionRepository = new VersionRepository();
        }
        return versionRepository;
    }

    public List<MicroVersion> getMicroVersion() {
        return unmodifiableList(MICRO_VERSIONS);
    }

    public static Optional<MicroVersion> toMicroVersion(String microVersion) {
        return MICRO_VERSIONS
                .stream()
                .filter(micro -> micro.getVersion().equals(microVersion))
                .findAny();
    }

    public String getJavaEEVersion(String microVersion) {
        return MICRO_VERSIONS.stream()
                .filter(micro -> micro.getVersion().equals(microVersion))
                .map(MicroVersion::getJavaeeVersion)
                .findAny().get();
    }

}
