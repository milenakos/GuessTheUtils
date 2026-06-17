pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.10-alpha.2"
}

stonecutter {
	create(rootProject) {
		versions("26.2")
		vcsVersion = "26.2"
	}
}

rootProject.name = "GuessTheUtils"
