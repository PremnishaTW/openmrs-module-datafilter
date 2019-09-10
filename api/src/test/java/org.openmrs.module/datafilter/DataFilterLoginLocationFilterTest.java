/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.LoginLocationFilter;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccessUtil.class, Context.class })
public class DataFilterLoginLocationFilterTest {
	
	private LoginLocationFilter filter = new DataFilterLoginLocationFilter();
	
	private static final Integer LOCATION_ID = 2;
	
	private AdministrationService as = mock(AdministrationService.class);
	
	@Before
	public void before() {
		mockStatic(AccessUtil.class);
		mockStatic(Context.class);
		when(AccessUtil.getAssignedBasisIds(eq(Location.class)))
		        .thenReturn(Stream.of("1", LOCATION_ID.toString()).collect(Collectors.toSet()));
		when(Context.getAdministrationService()).thenReturn(as);
		when(as.getGlobalProperty(eq(DataFilterLoginLocationFilter.GP_LOGIN_LOCATION_USER_PROPERTY)))
		        .thenReturn("some value");
	}
	
	@Test
	public void accept_shouldReturnFalseIfTheUserIsNotAssignedTheSpecifiedLocation() {
		assertFalse(filter.accept(new Location(3)));
	}
	
	@Test
	public void accept_shouldReturnTrueIfTheUserIsAssignedTheSpecifiedLocation() {
		assertTrue(filter.accept(new Location(LOCATION_ID)));
	}
	
	@Test
	public void accept_shouldReturnTrueIfTheUserLoginLocationPropertyIsNotSet() {
		when(as.getGlobalProperty(eq(DataFilterLoginLocationFilter.GP_LOGIN_LOCATION_USER_PROPERTY))).thenReturn(null);
		assertTrue(filter.accept(new Location(3)));
	}
	
}