package com.github.lankydan.palenightintellijtheme.services

import com.intellij.openapi.project.Project
import com.github.lankydan.palenightintellijtheme.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
