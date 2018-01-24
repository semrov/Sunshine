package com.semrov.jure.sunshine;

/**
 * Created by Jure on 17.12.2017.
 */


import com.semrov.jure.sunshine.data.TestDb;
import com.semrov.jure.sunshine.data.TestUtilities;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({TestDb.class})
public class UnitTestSuite {}
