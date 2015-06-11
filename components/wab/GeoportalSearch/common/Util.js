define([
  'dojo/topic'
],function(topic){
  return {

    subscribeMessages: function(callback) {
      return topic.subscribe("addLayer-message",callback);
    },
    
    publishMessage: function(message) {
      topic.publish("addLayer-message",message);
    },
    
    subscribeRecords: function(callback) {
      return topic.subscribe("addLayer-records",callback);
    },
    
    publishRecords: function(records) {
      topic.publish("addLayer-records",records);
    }

  }
});