package com.senjuid.camera

class CameraPluginOptions private constructor(
        val maxSize: Int?,
        val quality: Int?,
        val name: String?,
        val isFacingBack: Boolean?,
        val showFaceArea: Boolean?,
        val disableFacingBack: Boolean?,
        val disableMirroring: Boolean?,
        val snapshot: Boolean?
) {
    data class Builder(
            private var maxSize: Int? = 0,
            private var quality: Int? = 100,
            private var name: String? = "img_lite",
            private var isFacingBack: Boolean? = true,
            private var showFaceArea: Boolean? = false,
            private var disableFacingBack: Boolean? = false,
            private var disableMirroring: Boolean? = true,
            private var snapshot: Boolean? = true
    ) {
        fun setMaxSize(maxSize: Int) = apply { this.maxSize = maxSize }
        fun setQuality(quality: Int) = apply { this.quality = quality }
        fun setName(name: String) = apply { this.name = name }
        /**
         * Sets whether the camera is facing the back or front.
         *
         * @param facingBack true if the camera is facing the back, false if it's facing the front.
         * @return the modified instance of the class.
         * @see setShowFaceArea
         * @deprecated This method is not applicable when setDisableFacingBack is called. Please remove this method if setDisableFacingBack is used.
         */
        @Deprecated("This method is not applicable when setDisableFacingBack is true. Please remove this method if setDisableFacingBack is used.")
        fun setIsFacingBack(facingBack: Boolean = true) = apply { this.isFacingBack = facingBack }

        /**
         * Sets whether the face area should be visible or not.
         *
         * @param faceAreaVisible true if the face area should be visible, false otherwise.
         * @return the modified instance of the class.
         * @see setIsFacingBack
         */
        fun setShowFaceArea(faceAreaVisible: Boolean = true) = apply { this.showFaceArea = faceAreaVisible }

        /**
         * Sets whether to disable the functionality of facing back.
         *
         * @param disable true to disable the facing back functionality, false otherwise.
         * @return the modified instance of the class.
         */
        fun setDisableFacingBack(disable: Boolean) = apply { this.disableFacingBack = disable }
        fun setDisableMirroring(disable: Boolean) = apply { this.disableMirroring = disable }
        fun setSnapshot(snapshot: Boolean) = apply { this.snapshot = snapshot }
        fun build() = CameraPluginOptions(maxSize, quality, name, isFacingBack, showFaceArea, disableFacingBack, disableMirroring, snapshot)
    }
}