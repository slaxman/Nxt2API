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
 * Nxt transaction type
 */
public class TransactionType {

    /** Type */
    private final int type;

    /** Subtype */
    private final int subtype;

    /** Name */
    private final String name;

    /**
     * Create a new transaction type
     *
     * @param   type                Type
     * @param   subtype             Subtype
     * @param   name                Name
     */
    TransactionType(int type, int subtype, String name) {
        this.type = type;
        this.subtype = subtype;
        this.name = name;
    }

    /**
     * Get the hash code for this transaction type
     *
     * @return                      Hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(type) ^ Integer.hashCode(subtype);
    }

    /**
     * Check if two objects are equal
     *
     * @param   obj                 Object to check
     * @return                      TRUE if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof TransactionType) && type == ((TransactionType)obj).type &&
                subtype == ((TransactionType)obj).subtype);
    }
}
