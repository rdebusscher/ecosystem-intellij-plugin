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

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class MicroVersion {

    private final String version;

    private final String javaeeVersion;

    private final String displayName;

    public MicroVersion(String version, String javaeeVersion) {
        this.version = version;
        this.javaeeVersion = javaeeVersion;
        this.displayName = version;
    }

    public MicroVersion(String version, String javaeeVersion, String displayName) {
        this.version = version;
        this.javaeeVersion = javaeeVersion;
        this.displayName = displayName;
    }

    public String getVersion() {
        return version;
    }

    public String getJavaeeVersion() {
        return javaeeVersion;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
