/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.documentsui.inspector;

import static android.provider.DocumentsContract.Document.FLAG_SUPPORTS_SETTINGS;
import static com.android.internal.util.Preconditions.checkArgument;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.widget.LinearLayout;
import com.android.documentsui.DocumentsApplication;
import com.android.documentsui.R;
import com.android.documentsui.base.DocumentInfo;
import com.android.documentsui.roots.ProvidersAccess;
import java.util.function.Consumer;
/**
 * A controller that coordinates retrieving document information and sending it to the view.
 */
public final class InspectorController {

    private final Loader mLoader;
    private final Consumer<DocumentInfo> mHeader;
    private final Consumer<DocumentInfo> mDetails;
    private final Context mContext;
    private final ProvidersAccess mProviders;

    /**
     * InspectorControllerTest relies on this controller.
     */
    @VisibleForTesting
    public InspectorController(Context context, Loader loader, ProvidersAccess providers,
            Consumer<DocumentInfo> header, Consumer<DocumentInfo> details) {

        checkArgument(context != null);
        checkArgument(loader != null);
        checkArgument(providers != null);
        checkArgument(header != null);
        checkArgument(details != null);

        mContext = context;
        mLoader = loader;
        mProviders = providers;
        mHeader = header;
        mDetails = details;
    }

    public InspectorController(Context context, Loader loader, LinearLayout layout) {

        this(context,
                loader,
                DocumentsApplication.getProvidersCache (context),
                (HeaderView) layout.findViewById(R.id.inspector_header_view),
                (DetailsView) layout.findViewById(R.id.inspector_details_view));
    }

    public void reset() {
        mLoader.reset();
    }

    public void loadInfo(Uri uri) {
        mLoader.load(uri, this::updateView);
    }

    /**
     * Updates the view.
     */
    @Nullable
    private void updateView(@Nullable DocumentInfo docInfo) {
        if (docInfo == null) {
            return;
        }
        mHeader.accept(docInfo);
        mDetails.accept(docInfo);
    }

    /**
     * Shows the selected document in it's content provider.
     *
     * @param DocumentInfo whose flag FLAG_SUPPORTS_SETTINGS is set.
     */
    public void showInProvider(Uri uri) {

        Intent intent = new Intent(DocumentsContract.ACTION_DOCUMENT_SETTINGS);
        intent.setPackage(mProviders.getPackageName(uri.getAuthority()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    /**
     * Interface for loading document metadata.
     */
    public interface Loader {

        /**
         * Starts the Asynchronous process of loading file data.
         *
         * @param uri - A content uri to query metadata from.
         * @param callback - Function to be called when the loader has finished loading metadata. A
         * DocumentInfo will be sent to this method. DocumentInfo may be null.
         */
        void load(Uri uri, Consumer<DocumentInfo> callback);

        /**
         * Deletes all loader id's when android lifecycle ends.
         */
        void reset();
    }
}