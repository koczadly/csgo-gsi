module uk.oczadly.karl.csgogsi {
    
    exports uk.oczadly.karl.csgsi;
    
    exports uk.oczadly.karl.csgsi.state;
    exports uk.oczadly.karl.csgsi.state.components;
    
    exports uk.oczadly.karl.csgsi.config;
    
    
    //Dependencies
    requires transitive com.google.gson;
    requires slf4j.api;
    
}