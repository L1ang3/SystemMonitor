$(function () {
	var monitorChart = undefined;
	var OSinfo = undefined;
	var Diskinfo = undefined;

	var Networkinfo = undefined;
	var socket = undefined;
	var option = {
		title: {
			text: 'System usage'
		},
		tooltip: {
			trigger: 'axis',
			axisPointer: {
				type: 'cross',
				label: {
					backgroundColor: '#6a7985'
				}
			}
		},
		legend: {
			data: ['cpu%', 'mem%']
		},
		toolbox: {
			feature: {
				/*saveAsImage: {}*/
			}
		},
		grid: {
			left: '3%',
			right: '4%',
			bottom: '3%',
			containLabel: true
		},
		xAxis: [
			{
				type: 'time',
				boundaryGap: false,
			}
		],
		yAxis: [
			{
				type: 'value',
				// max:100,
				name: '',
			}
		],
		series: [
			{
				name: 'cpu%',
				type: 'line',
				symbol: 'none',
				data: []
			},
			{
				name: 'mem%',
				type: 'line',
				symbol: 'none',
				data: []
			}
		],
		color: ['#00A65A', '#c23632', '#367FA9']
	};
	var arrayIp = [];
	freshChart();

	function freshChart() {
		monitorChart = echarts.init(document.getElementById('monitorChart'));
		OSinfo = document.getElementById('OSinfo');
		Diskinfo = document.getElementById('Diskinfo');
		Networkinfo = document.getElementById('Networkinfo');
		monitorChart.setOption(option);
		monitorChartRefresh();
	}

	function monitorChartRefresh(newUrl) {
		if (typeof (WebSocket) == "undefined") {
			console.log("not support WebSocket");
		} else {
			console.log("support WebSocket");

			var remoteUrl = "http://" + window.location.host;
			if (newUrl !== undefined) {
				remoteUrl = newUrl
			}
			var wsUrl = remoteUrl.replace("http", "ws") + "/ws/monitor"

			socket = new WebSocket(wsUrl);
			socket.onopen = function () {
				console.log("Socket opened");
				socket.send("send message start(From Client)");
			};
			socket.onmessage = function (msg) {
				if (msg.data.indexOf("monitorCPU") > 0) {
					var result = JSON.parse(msg.data);
					while (option.series[0].data.length >= 300) {
						option.series[0].data.shift();
						option.series[1].data.shift();
					}
					option.series[0].data = option.series[0].data.concat(result.monitorCPU);
					option.series[1].data = option.series[1].data.concat(result.monitorMem);
					monitorChart.setOption({ series: option.series });
					OSinfo.textContent = result.monitorOS;

					// Update Diskinfo as table
					var diskData = result.monitorDisk;
					updateDiskTable(diskData);

					var networkData = result.monitorNetwork;
					updateNetworkTable(networkData);
				}
			};
			socket.onclose = function () {
				console.log("Socket closed");
			};
			socket.onerror = function () {
				alert("Socket error");
			}
		}
	}

	function updateDiskTable(diskData) {
		var diskTable = document.getElementById('diskTable').getElementsByTagName('tbody')[0];
		diskTable.innerHTML = ''; // Clear previous data
		var disks = diskData.split('Disk Name');
		for (var i = 1; i < disks.length; i++) {
			var rows = disks[i].split('\n').map(function (line) {
				return line.split(':').map(function (item) {
					return item.trim();
				});
			});

			var row = diskTable.insertRow();
			rows.forEach(function (data) {
				var cell = row.insertCell();
				if (data[0] == "Disk Read Bytes" || data[0] == "Disk Write Bytes") {
					cell.textContent = (data[1] / 1048576).toFixed(3);
				}
				else {
					cell.textContent = data[1];
				}
			});
		}
	}



	function updateNetworkTable(networkData) {
		var networkTable = document.getElementById('networkTable').getElementsByTagName('tbody')[0];
		networkTable.innerHTML = ''; // Clear previous data
		var interfaces = networkData.split('Network Interface Name: ');
		for (var i = 1; i < interfaces.length; i++) {
			var interfaceData = interfaces[i].split('\n');
			var row = networkTable.insertRow();
			var name = interfaceData[0].trim(); // Extract interface name
			var cell = row.insertCell();
			cell.textContent = name; // Display interface name
			interfaceData.splice(0, 1); // Remove interface name from data array
			interfaceData.forEach(function (data) {
				var cell = row.insertCell();
				var keyValue = data.split(':');
				var key = keyValue[0].trim();
				var value = keyValue.slice(1).join(':').trim();
				if (key === "MAC Address") {
					value = value.split(':').join(': '); // Add space after each colon for MAC address
				} else if (key === "IPv4 Address" || key === "IPv6 Address") {
					value = value.split('@')[1]; // Extract only the address part
				} else if (key == "Bytes Sent" || key === "Bytes Sent") {
					value /= 1048576;
					value = value.toFixed(3);
				} else if (key == "Packets Sent" || key == "Packets Received") {
					value /= 1000;
					value - value.toFixed(3);
				} else if (key == "Speed") {
					var number = value.match(/\d+/);
					number /= 1048576;

					value = number.toFixed(3) + " MBps";
				}
				cell.textContent = value;
			});
		}
	}



	window.onbeforeunload = function () {
		socket.close();
	}
	window.unload = function () {
		socket.close();
	};
});
