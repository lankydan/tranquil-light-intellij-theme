package com.github.lankydan.mysteryintellijtheme.services

import com.github.lankydan.mysteryintellijtheme.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
