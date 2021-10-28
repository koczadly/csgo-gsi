module uk.oczadly.karl.csgogsi {
    
    exports uk.oczadly.karl.csgsi;
    
    exports uk.oczadly.karl.csgsi.state;
    exports uk.oczadly.karl.csgsi.state.components;
    exports uk.oczadly.karl.csgsi.state.components.grenade;
    
    exports uk.oczadly.karl.csgsi.config;
    exports uk.oczadly.karl.csgsi.util.system;


    //Dependencies
    requires transitive com.google.gson;
    requires org.slf4j;
    
}