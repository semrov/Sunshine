package com.semrov.jure.sunshine;

/**
 * Created by Jure on 17.12.2017.
 */


import com.semrov.jure.sunshine.data.TestDb;
import com.semrov.jure.sunshine.data.TestProvider;
import com.semrov.jure.sunshine.data.TestUriMatcher;
import com.semrov.jure.sunshine.data.TestUtilities;
import com.semrov.jure.sunshine.data.TestWeatherContract;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({TestDb.class, TestProvider.class, TestUriMatcher.class, TestWeatherContract.class,TestFetchWeatherTask.class})
public class UnitTestSuite {}
