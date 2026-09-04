package com.example.groupassignment2app;

import static org.junit.Assert.assertEquals;

import com.example.groupassignment2app.data.Repo;

import org.junit.Test;

public class ExampleUnitTest {

    @Test
    public void chatIdIsTheSameBothWays() {
        String a = "aaa111";
        String b = "zzz999";
        assertEquals(Repo.chatIdFor(a, b), Repo.chatIdFor(b, a));
    }

    @Test
    public void chatIdSortsAlphabetically() {
        assertEquals("aaa111_zzz999", Repo.chatIdFor("zzz999", "aaa111"));
    }
}
