/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.io;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 * @author Mario A. Serrano Leones
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/appContext.xml"})
public class IOUtilsTest {

    public IOUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testGetResource() {
        String location = "classpath:my/res/resource1.txt";
        String expResult = "resource1.txt";
        Resource result = IOUtils.getResource(location);
        assertNotNull(result);
        assertEquals(expResult, result.getFilename());
    }

    @Test
    public void testGetResources() throws Exception {
        String location = "classpath:my/res/*.txt";
        int expResult = 2;
        Resource[] result = IOUtils.getResources(location);
        assertEquals(expResult, result.length);
    }

    @Test
    public void testGetAllResources() throws Exception {
        String location = "classpath:my/res/*.*";
        int expResult = 2;
        Resource[] result = IOUtils.getResources(location);
        assertEquals(expResult, result.length);
    }

    @Test
    public void testGetAllResourcesInMETA_INF() throws Exception {
        String location = "classpath:META-INF/res/*.*";
        int expResult = 2;
        Resource[] result = IOUtils.getResources(location);
        for (Resource resource : result) {
        }
        assertEquals(expResult, result.length);
    }

    @Test
    public void testScanPackage() throws IOException {
        String location = "classpath*:tools/dynamia/**/*.class";
        Resource[] result = IOUtils.getResources(location);
        assertNotNull(result);
        assertTrue(result.length > 0);
        for (Resource resource : result) {
        }
    }

    @Test
    public void testFileNameWithoutExtension() {
        File file = new File("TheFile_something.xml");
        String result = IOUtils.getFileNameWithoutExtension(file);
        String expected = "TheFile_something";
        assertEquals(expected, result);
    }

    @Test
    public void testFileExtension() {
        File file = new File("TheFile_something.xml");
        String result = IOUtils.getFileExtension(file);
        String expected = "xml";
        assertEquals(expected, result);
    }

}
