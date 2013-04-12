/**
 * @author Ammar Abdulamir
 * @file EntryNode.java
 * @brief A blackboard entry.
 * @date Created: 4/10/13
 * @section LICENSE
 * License: newBSD
 * Copyright © 2013, HU University of Applied Sciences Utrecht.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package rexos.client.blackboardreader.models;

import com.mongodb.BasicDBObject;

/**
 * A blackboard entry.
 **/
public class EntryNode {
    /**
     * The entry's key.
     *
     * @var String key
     **/
    private final String key;
    /**
     * The entry's value.
     *
     * @var String node
     **/
    private final Object node;

    /**
     * A constructor.
     *
     * @param bdo A BasicDBObject
     **/
    public EntryNode(BasicDBObject bdo) {
        this("Entry", bdo);
    }

    /**
     * A constructor.
     *
     * @param key The key
     * @param object The value
     **/
    public EntryNode(String key, Object object) {
        this.key = key;
        this.node = object;
    }

    /**
     * Gets a BasicDBObject.
     *
     * @return A BasicDBObject representation of the value.
     **/
    public BasicDBObject getNodeAsBdo() {
        return (BasicDBObject) node;
    }

    /**
     * Gets the value.
     *
     * @return The value
     **/
    public Object getNode() {
        return node;
    }

    /**
     * Gets the key.
     *
     * @return The key
     **/
    public String getKey() {
        return key;
    }

    /**
     * Check if current value is a BasicDBObject.
     *
     * @return Returns if the current value is a BasicDBObject
     **/
    public boolean isBdoNode() {
        return node instanceof BasicDBObject;
    }

    /**
     * eturns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        String ret;

        if (isBdoNode()) {
            ret = "[" + key + "]";
            if (getNodeAsBdo().containsField("_id"))
                ret += " {" + getNodeAsBdo().get("_id") + "}";
        } else
            ret = key + ": " + node;

        return ret;
    }
}