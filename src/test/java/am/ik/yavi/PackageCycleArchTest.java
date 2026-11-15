/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "am.ik.yavi", importOptions = ImportOption.DoNotIncludeTests.class)
class PackageCycleArchTest {

	@ArchTest
	static final ArchRule packagesAreFreeOfCycles = slices().matching("am.ik.yavi.(**)").should().beFreeOfCycles();

}
