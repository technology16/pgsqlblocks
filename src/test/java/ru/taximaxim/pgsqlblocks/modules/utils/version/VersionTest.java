package ru.taximaxim.pgsqlblocks.modules.utils.version;


import org.junit.Assert;
import org.junit.Test;
import ru.taximaxim.pgsqlblocks.utils.SupportedVersion;

public class VersionTest {

    @Test
    public void getNullVersion(){
        String text = "11.0.0";
        SupportedVersion versionDefault = SupportedVersion.get(text);
        Assert.assertEquals(SupportedVersion.VERSION_DEFAULT, versionDefault);
    }

    @Test
    public void getNumberversion(){
        String text = "10.0";
        SupportedVersion versionTen = SupportedVersion.get(text);
        Assert.assertEquals(SupportedVersion.VERSION_10, versionTen);
    }
}
