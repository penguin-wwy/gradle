/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.tooling.gradle10Milestone4

import org.gradle.integtests.tooling.ToolingApiSpecification
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject

class M4EclipseModelIntegrationTest extends ToolingApiSpecification {

    def "minimal Eclipse model does not attempt to resolve external dependencies"() {
        def projectDir = dist.testDir
        projectDir.file('settings.gradle').text = 'include "child"'
        projectDir.file('build.gradle').text = '''
apply plugin: 'java'
dependencies {
    compile project(':child')
    compile files { throw new RuntimeException() }
    compile 'this.lib.surely.does.not.exist:indeed:1.0'
}
project(':child') {
    apply plugin: 'java'
    dependencies {
        compile files { throw new RuntimeException() }
        compile 'this.lib.surely.does.not.exist:indeed:2.0'
    }
}
'''

        when:
        HierarchicalEclipseProject project = withConnection { connection -> connection.getModel(HierarchicalEclipseProject.class) }

        then:
        project.projectDependencies.size() == 1
        project.projectDependencies[0].path == 'child'
    }

    def "minimal Eclipse model does not attempt to call any tasks"() {
        def projectDir = dist.testDir
        projectDir.file('build.gradle').text = '''
apply plugin: 'java'

sourceSets.main.output.dir "$buildDir/foo", buildBy: 'generateResources'

task generateResources << {
  assert false : 'should not be called when building minimal model'
}
'''

        when:
        withConnection { connection -> connection.getModel(HierarchicalEclipseProject.class) }

        then:
        noExceptionThrown()
    }

}
