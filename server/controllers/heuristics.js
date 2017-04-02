var heuristics_controller = function(lat, lon, bus_no, bus_stop, direction, route_model,stop_model) {
	this.lat = lat;
	this.lon = lon;
	this.bus_no = bus_no.toUpperCase();
  this.bus_stop = bus_stop.toUpperCase();
  this.direction = direction;
  this.route_model = route_model;
  this.stop_model = stop_model;
	this.api_error_messages = require('../models/api_error_messages.js');
  this.find_travel_info = require('./google_distance.js')
};

heuristics_controller.prototype.query = function(callback){
  var me = this;

  me.route_model.findOne({
    bus_stop: me.bus_stop,
    bus_no: me.bus_no
  }, function(err, route) {

    if (err) {
      return callback(err, {
        success: false,
        payload: {
          msg: me.api_error_messages.database_error
        }
      });
    }

    // console.log(me.bus_stop);
    // console.log(me.bus_no);

    if(!route)
    {
      return callback(err, {
        success: false,
        payload: {
          msg: me.api_error_messages.bus_stop_not_found
        }
      });
    }

    var f = 0;
    var dt = new Date();
    var curr_time = dt.getSeconds() + (60 * dt.getMinutes()) + (60 * 60 * dt.getHours());

    for(var i=0;i<route.timings.length;i++)
    {
      if(route.timings[i]>curr_time){f=i;break;}
    }

    // console.log(f);
    // console.log(route.timings[f]);

    return callback(err, {
    success: true,
    payload:
    {
      bus_no: me.bus_no,
      bus_stop: me.bus_stop,
      time: route.timings[f]
    }
    });
  });
}

heuristics_controller.prototype.find_bus_stop_location = function(bus_no,stop_no, callback) {
  var me = this;

  me.route_model.findOne({
    bus_no: bus_no,
    stop_no: stop_no
  }, function(err, route) {

    if (err) {
      return callback(err, {
        success: false,
        payload: {
          msg: me.api_error_messages.database_error+" "+stop_no
        }
      });
    }

    else if(!route)
    {
      return callback(err, {
        success: false,
        payload: {
          msg:me.api_error_messages.bus_stop_not_found+" "+stop_no
        }
      });
    }

    // console.log(bus_no);
    // console.log(stop_no);
    // console.log(route);

    bus_stop = route.bus_stop;

    me.stop_model.findOne({
      stop_id: bus_stop
    }, function(err, stop) {
      if (err) {
        return callback(err, {
          success: false,
          payload: {
            msg: me.api_error_messages.database_error
          }
        });
      }
      if(stop)
      {
        return callback(err, {
        lat: stop.gps_lat,
        lon: stop.gps_lon
        });
     }

     return callback(err, {
       success: false,
       payload: {
         msg:me.api_error_messages.bus_stop_not_found
       }
     });

    });
  });
}

heuristics_controller.prototype.update_timing = function(stop_no, bus_no, time, callback){
  var me = this;

  me.route_model.findOne({
    stop_no: stop_no,
    bus_no: bus_no
  }, function(err, route) {

    if (err) {
      return callback(err, {
        success: false,
        payload: {
          msg: me.api_error_messages.database_error
        }
      });
    }

    // console.log(stop_no);
    // console.log(bus_no);

    if(!route)
    {
      return callback(err, {
        success: false,
        payload: {
          msg: "not found"
        }
      });
    }

    var f = 0;

    for(var i=0;i<route.timings.length;i++)
    {
      if(route.timings[i]>time){f=i-1;break;}
    }

    if(f==-1) f=route.timings.length-1;

    // console.log(f);
    // console.log(route.timings[f]);
    // console.log(time);

    route.timings[f] = 0.8*route.timings[f]+0.2*time;

    route.save(function(err){
      if(err){
          return callback(err, {
          success: false,
          payload: {
            msg: me.api_error_messages.database_error
          }
        });
      }
      else {
          return callback(err, {
          success: true,
          payload: route
        });
      }
    });
  });
}

heuristics_controller.prototype.update = function(callback) {

  var me = this;

  me.route_model.findOne({
    bus_no: me.bus_no,
    bus_stop: me.bus_stop
  }, function(err, route) {

    if (err) {
      return callback(err, {
        success: false,
        payload: {
          msg: me.api_error_messages.database_error
        }
      });
    }

    var bus_stop = route.bus_stop;
    var stop_no = route.stop_no;
    var f = 1;
    // console.log(route);

    // var allstops = [stop_no];
    // for(var i=stop_no;i<stop_no+20;i++){allstops.push(i);}
    //
    // var temp = allstops.reduce(function(t, s) {
    //   (function(i){
    //
    //       console.log(i);
    //
    //       var curr_time = new Date().getTime();
    //
    //       me.find_bus_stop_location(me.bus_no, i, function(err,resp){
    //
    //           console.log(resp);
    //
    //           if(resp.success==false)
    //           {
    //             return callback(err, {
    //               success: true,
    //               payload: {
    //                 msg: "success"
    //               }
    //             });
    //           }
    //           else
    //           {
    //             // var start = '12.935164,80.233645';
    //             // var end = '13.006806,80.240063';
    //             // console.log(me.lon);
    //             // console.log(me.lat);
    //
    //             end = resp.lat.toString()+","+resp.lon.toString();
    //             start = me.lat.toString()+","+me.lon.toString();
    //
    //             me.find_travel_info(start,end,'transit',function(err,resp){
    //                 if(err)
    //                 {
    //                   return callback(err, {
    //                     success: false,
    //                     payload: {
    //                       msg: me.api_error_messages.database_error
    //                     }
    //                   });
    //                 }
    //
    //                 travel_time = resp.durationValue;
    //
    //                 var updated_time = curr_time+travel_time;
    //
    //                 // console.log(updated_time);
    //
    //                 me.update_timing(i, me.bus_no, updated_time, function (err, resp) {
    //
    //                   console.log(resp);
    //
    //                   if (err) {
    //                     return callback(err, {
    //                       success: false,
    //                       payload: {
    //                         msg: me.api_error_messages.database_error
    //                       }
    //                     });
    //                   }
    //
    //                   f=1;
    //
    //                 });
    //             });
    //           }
    //       });
    //
    //       return false;
    //     })(s);
    // });

    for(var s=stop_no;s<stop_no+1;s++){

      (function(i){

          console.log(i);

          var dt = new Date();
          var curr_time = dt.getSeconds() + (60 * dt.getMinutes()) + (60 * 60 * dt.getHours());

          me.find_bus_stop_location(me.bus_no, i, function(err,resp){

              // console.log(resp);

              if(resp.success==false)
              {
                return callback(err, {
                  success: true,
                  payload: {
                    msg: "success"
                  }
                });
              }
              else
              {
                // var start = '12.935164,80.233645';
                // var end = '13.006806,80.240063';
                // console.log(me.lon);
                // console.log(me.lat);

                end = resp.lat.toString()+","+resp.lon.toString();
                start = me.lat.toString()+","+me.lon.toString();

                me.find_travel_info(start,end,'transit',function(err,resp){
                    if(err)
                    {
                      return callback(err, {
                        success: false,
                        payload: {
                          msg: me.api_error_messages.database_error
                        }
                      });
                    }

                    travel_time = resp.durationValue;

                    var updated_time = curr_time+travel_time;

                    // console.log(curr_time);
                    // console.log(travel_time);
                    // console.log(updated_time);

                    me.update_timing(i, me.bus_no, updated_time, function (err, resp) {

                      console.log(resp);

                      if (err) {
                        return callback(err, {
                          success: false,
                          payload: {
                            msg: me.api_error_messages.database_error
                          }
                        });
                      }
                    });
                });
              }
          });
        })(s);
      }

      return callback(err, {
        success: true,
        payload: {
          msg: "success"
        }
      });

  });
};

module.exports = heuristics_controller;