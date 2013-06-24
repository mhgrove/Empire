/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.lazyload;

import java.util.Date;

public interface Event
{
    public enum Status {
        New(0x10), Running(0x11), Complete(0x21), Failed(0x41), Canceled(0x42);

        public final int id;

        Status(int id) {
            this.id = id;
        }

        public static Status getStatus(int value) {
            return (value == New.id)? New:
                   (value == Running.id)? Running:
                   (value == Complete.id)? Complete:
                   (value == Failed.id)? Failed:
                   (value == Canceled.id)? Canceled: null;
        }
    }

    public String getSubject();
    public String getParameters();
    public Status getStatus();
    public String getOutcome();
    public Date getStartDate();
    public Date getEndDate();

    public void update(Status status, String outcome);
}
