package com.satotats

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.util.prevLeaf
import java.lang.String


class ClickAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println(e)
        println("heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeey")
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (file != null && editor != null) {
            if(file.text != "build.gradle" && file.text != "build.gradle.kts") return

            val element = file.findElementAt(editor.caretModel.offset)

            element!!.prevLeaf { it.text.equals("{") }!!.prevLeaf { it.text.equals("plugins") }
            println(String.format("\nSelected Element: %s", element?.text)
        }
    }
}