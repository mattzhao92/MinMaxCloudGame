
//Variable to keep track of player's current location for coloring
var currentLoc = null;

//Variable to keep track of opponent's current location for color
var oppLoc = null;

var GridTile = Class.extend({
	// Class constructor
	init: function(world, tileMesh, xPos, yPos, zheight) {
		this.world = world;
		this.tileMesh = tileMesh;
		this.xPos = xPos;
		this.yPos = yPos;
		this.zheight = zheight;
		this.isSelectable = true;
		this.free = true;
		this.isMovable = false;
		this.playerName = "None";
		this.tileMesh.material.opacity = 0.5;
		this.color = [Math.random(), Math.random(), Math.random()];
		this.tileMesh.material.color.setRGB(this.color[0], this.color[1], this.color[2]);
	},

	getTileMesh: function() {
		return this.tileMesh;
	},

	/**
	 * Updates the value of this tile
	 */
	updateValue: function(otherName, myName) {
		if (otherName ==  myName) {
			this.tileMesh.material.opacity = 0.5;
			this.tileMesh.material.color.setRGB(1, 1, 1);
			this.free = false;
		} else if (otherName == "None") {
			this.free = true;
			// do nothing in this case
		} else {

			this.tileMesh.material.opacity = 0.5;
			this.tileMesh.material.color.setRGB(0,0,0);
			this.free = false;
		}
	},

	/**
	 * Reset the properties of this tile
	 */
	reset: function() {
		this.isSelectable = false;
		this.isMovable = false;
		this.tileMesh.visible = false;
	},

	setSelectable: function(isSelectable) {
		this.isSelectable = isSelectable;
	},

	/**
	 * Set the tile's movable property
	 */
	setMovable: function(isMovable) {
		this.isMovable = isMovable;
	},

	/**
	 * Set the mouseover action
	 */
	onMouseOver: function(scope) {
		if (this.isSelectable) {
			//this.world.markTileAsSelected(this);
			return {
				x: this.xPos,
				z: this.yPos
			};
		}
		return null;
	}
});

var TileFactory = Class.extend({
	init: function(world, tileSize) {
		this.world = world;
		this.tileSize = tileSize;
	},

	/**
	 * Creates a new tile
	 */
	createTile: function(xPos, yPos, height) {
		this.tileGeom = new THREE.CubeGeometry(this.tileSize, height * 20, this.tileSize);
		var mat = new THREE.MeshBasicMaterial();
		var tile = new THREE.Mesh(this.tileGeom, mat);

		var gridCell = new GridTile(this.world, tile, xPos, yPos, height);
		tile.owner = gridCell;

		var tileMesh = gridCell.getTileMesh();
		tileMesh.position.x = this.world.convertXPosToWorldX(xPos);
		tileMesh.position.y = this.world.convertyPosToWorldZ(yPos);
		tileMesh.position.z = height * 10;
		tileMesh.rotation.x = -0.5 * Math.PI;

		tileMesh.visible = true;
		return gridCell;
	}
});


var GameView = Class.extend({

	//GameView(this, viewWidth, viewWidth, squareSize, this.camera, this.controls);
	init: function(model, dimension, board) {

		var currentScope = this;
		this.model = model;
		this.container = document.getElementById('container');

		this.viewWidth = $(window).width();//1000;// Math.min(container.clientWidth,container.clientHeight);
		this.viewHeight = $(window).height();//this.viewWidth;
		console.log(this.container);
		console.log("width " + this.viewWidth);
		console.log("height "+ this.viewHeight);
		
		this.cellSize = this.viewWidth / dimension;

		// set up three.js renderer
		this.renderer = new THREE.CanvasRenderer();
		this.renderer.setSize( this.viewWidth, this.viewHeight );
		this.container.appendChild(this.renderer.domElement );
		this.renderer.setClearColorHex( 0xeeeedd, 1.0 );

		// set up scene
		this.scene = new THREE.Scene();

		// set up object picking 

		this.projector = new THREE.Projector();
		this.mouseVector = new THREE.Vector3();


		// set up camera
		this.camera = new THREE.PerspectiveCamera( 45, this.viewWidth / this.viewHeight, 1, 10000 );
		this.camera.position.set( 0, 0, 1500);

		// set up controls
		this.controls = new THREE.MapControls(this.camera, this.scene, this.container);
        this.controls.panSpeed = 0.31;
        this.controls.rotateUp(-Math.PI/8);


		// initialize grid
		this.tile3D = new THREE.Object3D();
		this.num3D = new THREE.Object3D();
		this.tiles = [];

		this.numberSquaresOnXAxis = dimension;
		this.numberSquaresOnZAxis = dimension;

		this.tileFactory = new TileFactory(this, this.cellSize);
		for (var i = 0; i < this.numberSquaresOnXAxis; i++) {
			for (var j = 0; j < this.numberSquaresOnZAxis; j++) {
				var tile = this.tileFactory.createTile(i, j, board.cells[i * this.numberSquaresOnXAxis + j].val);

				var tileMesh = tile.getTileMesh();
				//this.tilesArray[i][j] = tile;
				this.tile3D.add(tileMesh);
				this.tiles.push(tile);
			}
		}     

		this.scene.add(this.tile3D);
		this.mouseListenersEnabled = false;

		// set up window listeners
        window.addEventListener('mousemove', function(event) {
        	if (currentScope.mouseListenersEnabled)
            	currentScope.onMouseMove(event);
        }, false);

        window.addEventListener('mousedown', function(event) {
        	if (currentScope.mouseListenersEnabled)
            	currentScope.onMouseDown(event);
        }, false);


        window.addEventListener( 'resize', function(event) {
        	currentScope.onWindowResize(event);
        }, false );

         window.addEventListener( 'keydown', function(event) {
         	if (currentScope.mouseListenersEnabled)
         	currentScope.onKeyDown(event);
         }, false );

		// start animations
		this.animate();
	},


	setPlayerName: function(name) {
		this.playerName = name;
	},

	enableMouseListeners: function() {
		this.mouseListenersEnabled = true;
	},

	disableMouseListeners: function() {
		this.mouseListenersEnabled = true;
	},

	/**
	 * Returns a board representation of the current tile values
	 */
	getBoard: function() {
		var board = [];
		for (var i = 0; i < this.tiles.length; i++) {
			var tile = this.tiles[i];
			board.push({x: tile.xPos, y:tile.yPos, val:tile.zheight, playerName: tile.playerName});
		}
		console.log(board);
		debugger;
		return board;
	},


	/**
	 * Updates the displayed board
	 */
	updateBoard: function(board) {
		var remoteCells = board.cells;
	

		for (var i = 0; i < remoteCells.length; i++) {
			var remoteCell = remoteCells[i];
			for (var j = 0; j < this.tiles.length; j++) {
				var localCell = this.tiles[j];
				if (remoteCell.x == localCell.xPos && localCell.yPos == remoteCell.y) {
					localCell.updateValue(remoteCell.playerName, this.playerName);
				}
			}
		}

	},

	onKeyDown: function(e) {
		if ( e.keyCode == 37 ) {
			this.moveCurrentPosition(-1, 0);
		} else  if ( e.keyCode == 38) {
			this.moveCurrentPosition(0, 1);
		} else if ( e.keyCode == 39 ) {
			this.moveCurrentPosition(1, 0);
		} else if ( e.keyCode == 40 ) {
			this.moveCurrentPosition(0, -1);
		}
	},

	moveToPosition: function(newX, newY) {
		var tile = this.tiles[newX * this.numberSquaresOnXAxis + newY];
		tile.playerName = this.playerName;
		tile.free = false;
		this.model.clickSquare(newY, newY);
	},
	
	moveCurrentPosition: function(deltaX, deltaY) {
		if (currentLoc == null) return;
		
		var oldX = currentLoc.owner.xPos;
		var oldY = currentLoc.owner.yPos;
		
		var tile = this.tiles[(oldX + deltaX) * this.numberSquaresOnXAxis + (oldY + deltaY)];
		if (tile == null) return;
		if (!tile.free) return;

		this.colorSelectedTile(tile);
		console.log("moveCurrentPosition");
		this.moveToPosition(tile.xPos, tile.yPos);
		
	},

	animate: function() {
		var me = this;
        requestAnimationFrame(function() {
            me.animate();
        });
		this.controls.update();
		this.renderer.render( this.scene, this.camera );
	},

	onWindowResize: function( e ) {
		this.viewWidth = container.clientWidth;
		this.viewHeight = container.clientHeight;
		this.renderer.setSize( this.viewWidth, this.viewHeight );
		this.camera.aspect = this.viewWidth / this.viewHeight;
		this.camera.updateProjectionMatrix();
	},

	onMouseMove: function(e) {
		var scope = this;
		this.mouseVector.x = 2 * (e.clientX / this.viewWidth) - 1;
			this.mouseVector.y = 1 - 2 * ( e.clientY / this.viewHeight );

			var raycaster = this.projector.pickingRay( this.mouseVector.clone(), this.camera ),
			intersects = raycaster.intersectObjects( this.tile3D.children);

			this.tile3D.children.forEach(function( cube ) {
				if (cube.owner != null && cube.owner.player == null)
					cube.material.opacity = 0.5;
			});
			
			if (this.previousHeightedCell != null) {
				var oldColor = this.previousHeightedCell.owner.color;
				this.previousHeightedCell.material.color.setRGB(oldColor[0],oldColor[1],oldColor[2]);
			}

			if (intersects.length > 0) {
				var intersection = intersects[ 0 ],
				obj = intersection.object;
				if (!obj.owner.free) return;
				this.previousHeightedCell = obj;
				obj.material.color.setRGB(0.0, 1, 0.0);

				//obj.material.opacity = 0.5;
				//obj.material.color.setRGB( 1.0 - 0 / intersects.length, 0, 0 );
			}
	},
	
	onMouseDown: function(e) {
		var scope = this;
		this.mouseVector.x = 2 * (e.clientX / this.viewWidth) - 1;
			this.mouseVector.y = 1 - 2 * ( e.clientY / this.viewHeight );

			var raycaster = this.projector.pickingRay( this.mouseVector.clone(), this.camera ),
			intersects = raycaster.intersectObjects( this.tile3D.children);

			this.tile3D.children.forEach(function( cube ) {
				if (cube.owner != null && cube.owner.player == null)
					cube.material.opacity = 0.5;
			});
			
			//Select a tile
			if (intersects.length > 0) {
				var intersection = intersects[ 0 ],
				obj = intersection.object;
				
				//Return if this tile is not free to be selected
				if (!obj.owner.free) return;
				
				this.colorSelectedTile(obj.owner);
				// //If this is the first move, sent this to be current location
				// if (currentLoc == null) {
				// 	currentLoc = obj;
				// }
				
				// //Set the location of the previous location to light grey
				// currentLoc.material.color.setRGB(0.8,0.8,0.8);
				

				// //Move player to new location and set current tile to white
				// currentLoc = obj;

				// this.moveToPosition(obj.owner.xPos, obj.owner.yPos);
				// obj.material.opacity = 0.5;
				// obj.material.color.setRGB( 1, 1, 1 );
				
				// obj.owner.free = false;
				// if (this.previousHeightedCell == obj) {
				// 	this.previousHeightedCell = null;
				// }
			}
	},

	colorSelectedTile: function(tile) {
			//If this is the first move, sent this to be current location
			if (currentLoc == null) {
				currentLoc = tile.getTileMesh();
			}
			
			//Set the location of the previous location to light grey
			currentLoc.material.color.setRGB(0.8,0.8,0.8);
			

			//Move player to new location and set current tile to white
			currentLoc = tile.getTileMesh();

			this.moveToPosition(tile.xPos, tile.yPos);
			tile.getTileMesh().material.opacity = 0.5;
			tile.getTileMesh().material.color.setRGB( 1, 1, 1 );
			
			tile.free = false;
			if (this.previousHeightedCell == tile.getTileMesh()) {
				this.previousHeightedCell = null;
			}
	},

	convertXPosToWorldX: function(tileXPos) {
		return -((this.viewWidth) / 2) + (tileXPos * this.cellSize);
	},

	convertyPosToWorldZ: function(tileyPos) {
		return -((this.viewHeight / 2)) + (tileyPos * this.cellSize);
	},
});

