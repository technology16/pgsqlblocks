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
