Every directory in res/blocks is searched for a blocks.json file. If one is found, it is read and all blocks added to
the game under the given manufacturer name. All default blocks are added to the game using this format, so take a look
at the existing maps for some examples.

Description of blocks.json format:
{ // accolades around entire file (as the JSON standard dictates)
  "manufacturer" : "/your company name, may include spaces/",
  "slogan" : "/a one-line description of your company/"

  "blocks" : { // all fields are optional unless stated otherwise
    "/name of the block, may include spaces/" : {
      "description" : "/informal description as shown to the player/"
      "graphic" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh used for drawing. Excludes any kind of studs
      "collision" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh used for collisions, may be the same as for drawing
      "size" : [/integer width x/, /integer width y/, /integer height z/], encompassing hitbox
      // if size is missing, it is calculated from collision. If collision is missing, a hitbox is created based on size. At least one of these is required.
      "mass" : /exact weight/, // in units of a 1x1x1 block
      "topPoints" : [ // coordinates where buds are. Note: this is one higher than where these buds are placed on
          [/integer x/, /integer y/, /integer z/] // multiple can be added, separated with commas
        ],
      "bottomPoints" : [ // coordinates where buds can be attached
          [/integer x/, /integer y/, /integer z/]
        ],
      "hidden" : /true or false/, // optional. When false or missing, this block is shown in the list.
    } // multiple blocks can be added, separated with commas
  }

  "joints" : { // all fields are required
    "/name of the joint, may include spaces/" : {
      "description" : "/informal description as shown to the player/",
      "bottom" : "/name of the bottom part as defined in "blocks"/", // usually, both bottom and top have ("hidden" : true)
      "top" : "/name of the top part as defined in "blocks"/",
      // for both top and bottom, also [/integer width x/, /integer width y/, /integer height z/] is accepted, where a basic block of the given dimensions is generated.
      "axis" : "/x, y or z/" // the axis to rotate around
      // when both minAngle and maxAngle are left out, the rotation is unlimited
      "minAngle" : /minimum angle/ // in degrees
      "maxAngle" : /maximum angle/ // in degrees
    }
  }

  "propellers" : {
    "/name of the block, may include spaces/" : {
      "description" : "/informal description as shown to the player/",
      "axis" : "/name of the axis mount as defined in "blocks"/",
      "propellerMesh" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh of the propeller
      "propellerOffset" : [/exact x/, /exact y/, /exact z/] // absolute offset of the propeller relative to the axis block origin
    }
  }

  "guns" : {
    "/name of the block, may include spaces/" : { // all fields are required unless stated otherwise
      "description" : "/informal description as shown to the player/",
      "base" : "/name of the bottom part as defined in "blocks"/", // the static part of the gun
      "barrel" : "/name of the barrel part as defined in "blocks"/", // rotates both vertical and (indirectly) horizontal
      "horizontalAngle" : /exact maximum horizontal angular movement of the top part/, // in degrees, to left + to right. Defaults to 0
      "verticalMinAngle" : /exact minimum vertical angle of the top part/, // in degrees
      "verticalMaxAngle" : /exact maximum vertical angle of the top part/, // in degrees
      "reloadTime" : /exact time between shots/, // in seconds
      "rotationSpeed" : /exact aiming speed/, // in degrees/second, for both horizontal as vertical
      "jointOffset" : [/exact x/, /exact y/, /exact z/] // center of barrel rotations. Defaults to (0, 0, 0)
    }
  }
}
End description of blocks.json format. Order of components within a block does not matter.
