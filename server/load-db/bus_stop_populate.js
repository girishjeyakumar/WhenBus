var stop_model = require("../models/bus_stop.js")

var mongoose = require("mongoose")


// Clear the collection for bus stops
stop_model.remove({ }, function(err) {
    if (!err) {
		console.log("cleared bus stops successfully")
    }
    else {
		console.log("Error occured when clearing bus stops database")
    }
});

// Populate the Bus Stops collection
stps = []

stps.push(new stop_model ({
		stop_id: "1",
		stop_name: "Jamuna Hostel",
		gps_x: 12.986616,
		gps_y: 80.238765
}));

stps.push(new stop_model ({
		stop_id: "2",
		stop_name: "Narmada Hostel",
		gps_x: 12.986492,
		gps_y: 80.235329
}));

stps.push(new stop_model ({
		stop_id: "3",
		stop_name: "Taramani Guest House",
		gps_x: 12.986586,
		gps_y: 80.233226
}));

stps.push(new stop_model ({
		stop_id: "4",
		stop_name: "CRC Back",
		gps_x: 12.988173,
		gps_y: 80.230096
}));

stps.push(new stop_model ({
		stop_id: "5",
		stop_name: "BT Department",
		gps_x: 12.990013,
		gps_y: 80.227703
}));

stps.push(new stop_model ({
		stop_id: "6",
		stop_name: "Velachery Gate",
		gps_x: 12.988455,
		gps_y: 80.223369
}));

stps.push(new stop_model ({
		stop_id: "7",
		stop_name: "BT Department 2",
		gps_x: 12.990274,
		gps_y: 80.227574
}));

stps.push(new stop_model ({
		stop_id: "8",
		stop_name: "CRC Front",
		gps_x: 12.990839,
		gps_y: 80.230063
}));

stps.push(new stop_model ({
		stop_id: "9",
		stop_name: "HSB",
		gps_x: 12.991079,
		gps_y: 80.232123
}));

stps.push(new stop_model ({
		stop_id: "10",
		stop_name: "Gajendra Circle",
		gps_x: 12.991790,
		gps_y: 80.233765
}));

stps.push(new stop_model ({
		stop_id: "11",
		stop_name: "Post Office",
		gps_x: 12.993675,
		gps_y: 80.234280
}));

stps.push(new stop_model ({
		stop_id: "12",
		stop_name: "Children's Park",
		gps_x: 12.996051,
		gps_y: 80.236006
}));

stps.push(new stop_model ({
		stop_id: "13",
		stop_name: "Vana Vani School",
		gps_x: 12.998523,
		gps_y: 80.239108
}));

stps.push(new stop_model ({
		stop_id: "14",
		stop_name: "Residences Bus Stop",
		gps_x: 13.002568,
		gps_y: 80.240052
}));

stps.push(new stop_model ({
		stop_id: "15",
		stop_name: "Main Gate",
		gps_x: 13.005971,
		gps_y: 80.242512
}));

stps.forEach(function(stp){
	stp.save(function (err, stp) {
	  if (err) return console.error(err);
		console.log("Saved bus stop id : " +  stp.stop_id +  " to db")
	});
});

mongoose.connection.close()