/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
 * %
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
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.modules.utils;

import org.junit.Assert;
import org.junit.Test;
import ru.taximaxim.pgsqlblocks.utils.SupportedVersion;

import java.util.Optional;

public class SupportedVersionTest {

    @Test
    public void getByNameMissingTest(){
        String text = "11.0.0";
        Optional<SupportedVersion> versionDefault = SupportedVersion.getByVersionName(text);
        Assert.assertTrue(!versionDefault.isPresent());
    }

    @Test
    public void getByNameExistingTest(){
        String text = "10.0";
        Optional<SupportedVersion> versionTen = SupportedVersion.getByVersionName(text);
        Assert.assertTrue(versionTen.isPresent());
        Assert.assertEquals(SupportedVersion.VERSION_10, versionTen.get());
    }

    @Test
    public void getByNumber930Test(){
        int versionNumber = 90300;
        Optional<SupportedVersion> versionOpt = SupportedVersion.getByVersionNumber(versionNumber);
        Assert.assertTrue(versionOpt.isPresent());
        Assert.assertEquals(SupportedVersion.VERSION_9_3, versionOpt.get());
    }

    @Test
    public void getByNumber935Test(){
        int versionNumber = 90305;
        Optional<SupportedVersion> versionOpt = SupportedVersion.getByVersionNumber(versionNumber);
        Assert.assertTrue(versionOpt.isPresent());
        Assert.assertEquals(SupportedVersion.VERSION_9_3, versionOpt.get());
    }
}
