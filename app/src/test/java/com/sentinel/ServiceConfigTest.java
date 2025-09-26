package com.sentinel;

import android.content.Context;
import android.content.res.Resources;

import com.sentinel.config.ServiceConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class ServiceConfigTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private Resources mockResources;
    
    private ServiceConfig serviceConfig;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Use Robolectric context for actual testing
        Context realContext = RuntimeEnvironment.getApplication();
        serviceConfig = new ServiceConfig(realContext);
    }
    
    @Test
    public void testGetTargetPackage() {
        String targetPackage = serviceConfig.getTargetPackage();
        assertNotNull("Target package should not be null", targetPackage);
        assertEquals("Target package should be com.whatsapp", "com.whatsapp", targetPackage);
    }
    
    @Test
    public void testGetLogTag() {
        String logTag = serviceConfig.getLogTag();
        assertNotNull("Log tag should not be null", logTag);
        assertEquals("Log tag should be Sentinel", "Sentinel", logTag);
    }
    
}
