# Introduction

This is a custom bot made for the OSRS clan Irons Tavern. It is meant to be a bot that has custom capabilities for anything that we might need.

# Automatic Setup

Under **dev.xjade.tavern.maid.utilities.generators** you can run "InitialSetup" to run the initial setup. You will then need to manually copy over and edit any config files from example_configs. 

# Manual Setup

There are a couple of generators in the class. These are found in **dev.xjade.tavern.maid.utilities.generators**

You should run every single class suffixed with Generator.

To setup the database, run **liquibase:update**

You should then run **jooq-codegen:generate** which will generate code based on the database schemas.