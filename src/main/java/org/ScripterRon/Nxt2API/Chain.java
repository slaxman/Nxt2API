/*
 * Copyright 2017 Ronald W Hoffman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.Nxt2API;

/**
 * Nxt chain description
 */
public class Chain {

    /** Chain name */
    private final String name;

    /** Chain identifier */
    private final int id;

    /** Number of decimals */
    private final int decimals;

    /**
     * Create a chain description
     *
     * @param   name            Chain name
     * @param   id              Chain identifier
     * @param   decimals        Number of decimal places
     */
    public Chain(String name, int id, int decimals) {
        this.name = name;
        this.id = id;
        this.decimals = decimals;
    }

    /**
     * Return the chain name
     *
     * @return                  Chain name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the chain identifier
     *
     * @return                  Chain identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Return the number of decimal places
     *
     * @return                  Decimal places
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * Get the hash code
     *
     * @return                  Hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Check if the supplied object is equal to this one
     *
     * @param   obj             Object to compare
     * @return                  TRUE if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof Chain) && id == ((Chain)obj).id);
    }
}
