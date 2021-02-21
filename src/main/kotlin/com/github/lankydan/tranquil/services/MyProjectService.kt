package com.github.lankydan.tranquil.services

import com.github.lankydan.tranquil.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
