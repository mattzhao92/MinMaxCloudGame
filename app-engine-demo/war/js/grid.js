var GridTile = Class.extend({
	// Class constructor
	init: function(world, tileMesh, xPos, yPos) {
		this.world = world;
		this.tileMesh = tileMesh;
		this.xPos = xPos;
		this.yPos = yPos;
		this.isSelectable = true;
		this.isMovable = false;
		this.tileMesh.material.opacity = 0.5;
		this.tileMesh.material.color.setRGB( Math.random(), Math.random(), Math.random());
	},

	getTileMesh: function() {
		return this.tileMesh;
	},

	reset: function() {
		this.isSelectable = false;
		this.isMovable = false;
		this.tileMesh.visible = false;
	},

	markAsNotSelected: function() {
		if (this.isMovable) {
			this.highlight("GREEN");
		}
	},

	highlight: function(color) {

		this.tileMesh.visible = true;
		this.tileMesh.material.opacity = 0.6;
		var rgb;
		switch (color) {
		case "GREEN":
			rgb = [0.1, 1.0, 0.1];
			break;
		case "YELLOW":
			rgb = [3.0, 3.0, 0];
			break;
		case "RED": 
			rgb = [1.0, 0, 0];
			break;
		default:
			console.log("Invalid color specified");
		break;
		}

		this.tileMesh.material.color.setRGB(rgb[0], rgb[1], rgb[2]);
	},

	setSelectable: function(isSelectable) {
		this.isSelectable = isSelectable;
	},

	setMovable: function(isMovable) {
		this.isMovable = isMovable;
	},

	onMouseOver: function(scope) {
		if (this.isSelectable) {
			//this.world.markTileAsSelected(this);
			return {
				x: this.xPos,
				z: this.yPos
			};
		}
		return null;
	},

	markAsMovable: function() {
		if (this.isMovable) {
			this.highlight("GREEN");
		}
	}
});

var TileFactory = Class.extend({
	init: function(world, tileSize) {
		console.log(tileSize);
		this.world = world;
		this.tileSize = tileSize;
	},

	createTile: function(xPos, yPos) {
		var height = 1+Math.floor(Math.random() * 200);

		this.tileGeom = new THREE.CubeGeometry(this.tileSize, height, this.tileSize);
		var mat = new THREE.MeshBasicMaterial();
		var tile = new THREE.Mesh(this.tileGeom, mat);

		var gridCell = new GridTile(this.world, tile, xPos, yPos);
		tile.owner = gridCell;

		var tileMesh = gridCell.getTileMesh();

		tileMesh.position.x = this.world.convertXPosToWorldX(xPos);
		tileMesh.position.y = this.world.convertyPosToWorldZ(yPos);
		tileMesh.position.z = height/2;
		tileMesh.rotation.x = -0.5 * Math.PI;

		tileMesh.visible = true;
		return gridCell;
	}
});


var GameView = Class.extend({

	//GameView(this, viewWidth, viewWidth, squareSize, this.camera, this.controls);
	init: function(model, dimension) {
		var currentScope = this;
		this.model = model;
		this.container = document.getElementById('container');
		
		this.viewWidth = Math.min(container.clientWidth,container.clientHeight);
		this.viewHeight = this.viewWidth;

		this.cellSize = this.viewWidth / dimension;

		console.log("width height "+this.viewWidth + " "+this.viewHeight);
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
		this.camera.position.set( 0, 0, 1200);

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

		console.log(this.numberSquaresOnXAxis);
		console.log(this.numberSquaresOnZAxis);
		for (var i = 0; i < this.numberSquaresOnXAxis; i++) {
			for (var j = 0; j < this.numberSquaresOnZAxis; j++) {
				var tile = this.tileFactory.createTile(i, j);

				var tileMesh = tile.getTileMesh();
				//this.tilesArray[i][j] = tile;
				this.tile3D.add(tileMesh);
				this.tiles.push(tile);
			}
		}     

		this.scene.add(this.tile3D);
		// set up window listeners
        window.addEventListener('mousemove', function(event) {
            currentScope.onMouseMove(event);
        }, false);

        window.addEventListener( 'resize', function(event) {
        	currentScope.onWindowResize(event);
        }, false );

        // window.addEventListener( 'keydown', function(event) {
        // 	currentScope.onKeyDown(event);
        // }, false );

		// start animations
		this.animate();
	},

	onKeyDown: function(e) {
		console.log("e "+e.keyCode);
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
		this.model.clickSquare({'x': newY, 'y':newY});
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

			if (intersects.length > 0) {
				var intersection = intersects[ 0 ],
				obj = intersection.object;
				obj.material.color.setRGB(0.0, 1, 0.0);
				this.moveToPosition(obj.owner.xPos, obj.owner.yPos);
				//obj.material.opacity = 0.5;
				//obj.material.color.setRGB( 1.0 - 0 / intersects.length, 0, 0 );
			}
	},
	
	convertXPosToWorldX: function(tileXPos) {
		return -((this.viewWidth) / 2) + (tileXPos * this.cellSize);
	},

	convertyPosToWorldZ: function(tileyPos) {
		return -((this.viewHeight / 2)) + (tileyPos * this.cellSize);
	},
});

