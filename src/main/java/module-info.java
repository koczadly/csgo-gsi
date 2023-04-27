module uk.oczadly.karl.csgogsi {

    exports uk.oczadly.karl.csgsi.state;
    exports uk.oczadly.karl.csgsi.state.components;
    exports uk.oczadly.karl.csgsi.state.components.grenade;
    exports uk.oczadly.karl.csgsi.state.context;
    
    exports uk.oczadly.karl.csgsi.config;

    exports uk.oczadly.karl.csgsi.server;
    exports uk.oczadly.karl.csgsi.server.listener;
    exports uk.oczadly.karl.csgsi.server.handler;
    exports uk.oczadly.karl.csgsi.server.filter;
    exports uk.oczadly.karl.csgsi.server.serialization;

    exports uk.oczadly.karl.csgsi.util.game;


    //Dependencies
    requires transitive com.google.gson;
    requires org.slf4j;
    
}