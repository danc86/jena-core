/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.assembler.test;


import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.MemoryModelAssembler;
import com.hp.hpl.jena.mem.GraphMemBase;
import com.hp.hpl.jena.rdf.model.*;

public class TestMemoryModelAssembler extends ModelAssemblerTestBase
    {
    public TestMemoryModelAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return MemoryModelAssembler.class; }
    
    public void testMemoryModelAssemblerType()
        { testDemandsMinimalType( new MemoryModelAssembler(), JA.MemoryModel );  }
    
    public void testMemoryModelAssembler()
        {
        Assembler a = new MemoryModelAssembler();
        Model m = a.openModel( resourceInModel( "x rdf:type ja:MemoryModel" ) );
        assertInstanceOf( Model.class, m );
        assertInstanceOf( GraphMemBase.class, m.getGraph() );
        }
    
    public void testCreatesWithStyle()
        {
        testCreatesWithStyle( new MemoryModelAssembler(), "x rdf:type ja:MemoryModel" );
        }
    }
