/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.osgi.installer.impl;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.sling.osgi.installer.InstallableResource;
import org.apache.sling.osgi.installer.impl.tasks.BundleInstallRemoveTask;
import org.apache.sling.osgi.installer.impl.tasks.BundleStartTask;
import org.apache.sling.osgi.installer.impl.tasks.ConfigInstallRemoveTask;
import org.apache.sling.osgi.installer.impl.tasks.SynchronousRefreshPackagesTask;

/** Test the ordering and duplicates elimination of
 * 	OsgiControllerTasks
 */
public class TaskOrderingTest {

	private Set<OsgiControllerTask> taskSet;
	
	@org.junit.Before public void setUp() {
	    // The data type must be consistent with the "tasks" member
	    // of the {@link OsgiControllerImpl} class.
		taskSet = new TreeSet<OsgiControllerTask>(); 
	}
	
	private static RegisteredResource getRegisteredResource(String url) throws IOException {
		return new RegisteredResource(null, new InstallableResource(url, new Hashtable<String, Object>()));
	}
	
	private void assertOrder(int testId, Collection<OsgiControllerTask> actual, OsgiControllerTask [] expected) {
		int index = 0;
		for(OsgiControllerTask t : actual) {
			if(!t.equals(expected[index])) {
				fail("Test " + testId + ": at index " + index + ", expected " + expected[index] + " but got " + t);
			}
			index++;
		}
	}
	
	@org.junit.Test 
	public void testBasicOrdering() throws Exception {
		int testIndex = 1;
		final OsgiControllerTask [] tasksInOrder = {
			new ConfigInstallRemoveTask("someURI.cfg", null, null),
			new ConfigInstallRemoveTask("someURI.cfg", getRegisteredResource("someURI.cfg"), null),
			new BundleInstallRemoveTask("someURI", null, null, null),
			new BundleInstallRemoveTask("someURI", getRegisteredResource("someURI.jar"), null, null),
			new SynchronousRefreshPackagesTask(),
			new BundleStartTask(0),
		};
	
		taskSet.clear();
		taskSet.add(tasksInOrder[5]);
		taskSet.add(tasksInOrder[4]);
		taskSet.add(tasksInOrder[3]);
		taskSet.add(tasksInOrder[2]);
		taskSet.add(tasksInOrder[1]);
		taskSet.add(tasksInOrder[0]);
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
		
		taskSet.clear();
		taskSet.add(tasksInOrder[0]);
		taskSet.add(tasksInOrder[1]);
		taskSet.add(tasksInOrder[2]);
		taskSet.add(tasksInOrder[3]);
		taskSet.add(tasksInOrder[4]);
		taskSet.add(tasksInOrder[5]);
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
		
		taskSet.clear();
		taskSet.add(tasksInOrder[1]);
		taskSet.add(tasksInOrder[0]);
		taskSet.add(tasksInOrder[3]);
		taskSet.add(tasksInOrder[2]);
		taskSet.add(tasksInOrder[5]);
		taskSet.add(tasksInOrder[4]);
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
		
		taskSet.clear();
		taskSet.add(tasksInOrder[2]);
		taskSet.add(tasksInOrder[3]);
		taskSet.add(tasksInOrder[4]);
		taskSet.add(tasksInOrder[5]);
		taskSet.add(tasksInOrder[0]);
		taskSet.add(tasksInOrder[1]);
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
	}
	
	@org.junit.Test 
	public void testMultipleConfigAndBundles() throws Exception {
		int testIndex = 1;
		final OsgiControllerTask [] tasksInOrder = {
			new ConfigInstallRemoveTask("someURIa.cfg", null, null),
			new ConfigInstallRemoveTask("someURIb.cfg", null, null),
			new ConfigInstallRemoveTask("someURIa.cfg", getRegisteredResource("someURIa.cfg"), null),
			new ConfigInstallRemoveTask("someURIb.cfg", getRegisteredResource("someURIb.cfg"), null),
			new BundleInstallRemoveTask("someURIa.jar", null, null, null),
			new BundleInstallRemoveTask("someURIb.jar", null, null, null),
			new BundleInstallRemoveTask("someURIa.jar", getRegisteredResource("someURIa.jar"), null, null),
			new BundleInstallRemoveTask("someURIb.jar", getRegisteredResource("someURIb.jar"), null, null),
			new SynchronousRefreshPackagesTask(),
			new BundleStartTask(0),
		};
	
		taskSet.clear();
		for(int i = tasksInOrder.length -1 ; i >= 0; i--) {
			taskSet.add(tasksInOrder[i]);
		}
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
	}
	
	@org.junit.Test 
	public void testMultipleRefreshAndStart() throws Exception {
		int testIndex = 1;
		final OsgiControllerTask [] tasksInOrder = {
			new ConfigInstallRemoveTask("someURI.cfg", null, null),
			new ConfigInstallRemoveTask("someURI.cfg", getRegisteredResource("someURI.cfg"), null),
			new BundleInstallRemoveTask("someURI", null, null, null),
			new BundleInstallRemoveTask("someURI", getRegisteredResource("someURI.jar"), null, null),
			new SynchronousRefreshPackagesTask(),
			new BundleStartTask(0),
			new BundleStartTask(1),
		};
		
		taskSet.clear();
		taskSet.add(tasksInOrder[6]);
		taskSet.add(tasksInOrder[6]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[5]);
		taskSet.add(tasksInOrder[5]);
		taskSet.add(tasksInOrder[4]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[4]);
		taskSet.add(tasksInOrder[4]);
		taskSet.add(tasksInOrder[3]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[3]);
		taskSet.add(tasksInOrder[2]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[2]);
		taskSet.add(tasksInOrder[1]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[1]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[0]);
		taskSet.add(new SynchronousRefreshPackagesTask());
		taskSet.add(tasksInOrder[0]);
		
		assertOrder(testIndex++, taskSet, tasksInOrder);
	}
	
	@org.junit.Test 
	public void testBundleStartOrder() {
		int testIndex = 1;
		final OsgiControllerTask [] tasksInOrder = {
			new BundleStartTask(0),
			new BundleStartTask(1),
			new BundleStartTask(5),
			new BundleStartTask(11),
			new BundleStartTask(51)
		};
		
		taskSet.clear();
		for(int i = tasksInOrder.length -1 ; i >= 0; i--) {
			taskSet.add(tasksInOrder[i]);
		}
		assertOrder(testIndex++, taskSet, tasksInOrder);
	}
}
