/*
 * Copyright 2013, 2014, 2015 Antoine Vianey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.avianey.androidsvgdrawable;

import javax.annotation.Nullable;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Set the override mode for generated files.
 *
 * @author antoine vianey
 */
public enum OverwriteMode {

    always, never, ifModified;

    public boolean shouldOverride(File src, File dest, @Nullable File intermediate) {
        checkNotNull(src, "Source file MUST not be null");
        checkNotNull(dest, "Destination path MUST not be null");
        if (dest.lastModified() == 0 || always.equals(this)) {
            // always re-create or destination file does not exists
            return true;
        } else if (ifModified.equals(this)) {
            return src.lastModified() > dest.lastModified()
                    || (intermediate != null && intermediate.lastModified() > dest.lastModified());
        }
        return false;
    }

}
