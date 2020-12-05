package com.github.lankydan.palenightintellijtheme.services

import com.github.lankydan.palenightintellijtheme.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
