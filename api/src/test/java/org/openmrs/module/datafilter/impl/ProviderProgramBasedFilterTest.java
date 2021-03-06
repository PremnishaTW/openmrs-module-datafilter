/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.FilterTestUtils;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class ProviderProgramBasedFilterTest extends BaseProgramBasedFilterTest {
	
	@Autowired
	private ProviderService providerService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "providers.xml");
	}
	
	private Collection<Provider> getProviders() {
		return providerService.getProviders("Mulemba", null, null, null, true);
	}
	
	@Test
	public void getProviders_shouldExcludeProvidersWithProgramRolesForAUserThatHasNoRoles() {
		reloginAs("smulemba", "test");
		assertEquals(0, Context.getAuthenticatedUser().getAllRoles().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		Collection<Provider> providers = getProviders();
		Collection<Provider> userProviderAccounts = providerService
		        .getProvidersByPerson(Context.getAuthenticatedUser().getPerson());
		Context.removeProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		assertEquals(1, userProviderAccounts.size());
		assertNull(userProviderAccounts.iterator().next().getRole());
		assertEquals(2, providers.size());
		assertTrue(TestUtil.containsId(providers, 100005));
		assertTrue(TestUtil.containsId(providers, 100006));
	}
	
	@Test
	public void getProviders_shouldExcludeProvidersWithProgramRolesForAUserThatHasNoProgramRoles() {
		reloginAs("tmulemba", "test");
		Collection<Role> userRoles = Context.getAuthenticatedUser().getAllRoles();
		assertTrue(userRoles.size() > 0);
		Collection<String> programRoles = AccessUtil.getAllProgramRoles();
		assertEquals(0,
		    userRoles.stream().filter(r -> programRoles.contains(r.getName())).collect(Collectors.toList()).size());
		Collection<Provider> providers = getProviders();
		assertEquals(2, providers.size());
		assertTrue(TestUtil.containsId(providers, 100005));
		assertTrue(TestUtil.containsId(providers, 100006));
	}
	
	@Test
	public void getProviders_shouldReturnProvidersWithAccessToTheSameProgramsAsTheAuthenticatedUser() {
		reloginAs("cmulemba", "test");
		int expCount = 6;
		Collection<Provider> providers = getProviders();
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 100001));
		assertTrue(TestUtil.containsId(providers, 100002));
		//Should include a user working at the same program but in a different uncommon role
		assertTrue(TestUtil.containsId(providers, 100004));
		//Should include a provider with no roles
		assertTrue(TestUtil.containsId(providers, 100005));
		//Should include a user with some other role(s) but none is a program role
		assertTrue(TestUtil.containsId(providers, 100006));
		//Should include a user with any of the roles the user has
		assertTrue(TestUtil.containsId(providers, 100007));
		
		service.grantAccess(new Role(ROLE_COORDINATOR_PROG_1), new Program(10002));
		expCount = 7;
		providers = getProviders();
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 100001));
		assertTrue(TestUtil.containsId(providers, 100002));
		assertTrue(TestUtil.containsId(providers, 100003));
		assertTrue(TestUtil.containsId(providers, 100004));
		assertTrue(TestUtil.containsId(providers, 100005));
		assertTrue(TestUtil.containsId(providers, 100006));
		assertTrue(TestUtil.containsId(providers, 100007));
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(7, getProviders().size());
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfTheFilterIsDisabled() {
		FilterTestUtils.disableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_PROVIDER);
		reloginAs("dyorke", "test");
		assertEquals(7, getProviders().size());
	}
	
}
